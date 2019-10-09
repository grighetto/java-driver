/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.cql.continuous;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.Timer;
import com.datastax.dse.driver.DseSessionMetric;
import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.dse.driver.api.core.config.DseDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.cql.BatchStatement;
import com.datastax.oss.driver.api.core.cql.DefaultBatchType;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.metrics.DefaultNodeMetric;
import com.datastax.oss.driver.api.core.metrics.Metrics;
import com.tngtech.java.junit.dataprovider.DataProvider;
import java.util.UUID;

public abstract class ContinuousPagingITBase {

  protected static final String KEY = "k";

  static PreparedStatement prepared;

  protected static void initialize(DseSession session, DriverExecutionProfile slowProfile) {
    session.execute(
        SimpleStatement.newInstance("CREATE TABLE test (k text, v int, PRIMARY KEY (k, v))")
            .setExecutionProfile(slowProfile));
    // Load enough rows to cause TCP Zero Window. Default window size is 65535 bytes, each row
    // is at least 48 bytes, so it would take ~1365 enqueued rows to zero window.
    // Conservatively load 20k rows.
    session.execute(
        SimpleStatement.newInstance(
                "CREATE TABLE test_autoread (k text, v int, v0 uuid, v1 uuid, PRIMARY KEY (k, v, v0))")
            .setExecutionProfile(slowProfile));
    session.execute(
        SimpleStatement.newInstance("CREATE TABLE test_prepare (k text PRIMARY KEY, v int)")
            .setExecutionProfile(slowProfile));
    session.checkSchemaAgreement();
    prepared = session.prepare("SELECT v from test where k = ?");
    for (int i = 0; i < 100; i++) {
      session.execute(String.format("INSERT INTO test (k, v) VALUES ('%s', %d)", KEY, i));
    }
    int count = 0;
    for (int i = 0; i < 200; i++) {
      BatchStatement batch = BatchStatement.newInstance(DefaultBatchType.UNLOGGED);
      for (int j = 0; j < 100; j++) {
        batch =
            batch.add(
                SimpleStatement.newInstance(
                    "INSERT INTO test_autoread (k, v, v0, v1) VALUES (?, ?, ?, ?)",
                    KEY,
                    count++,
                    UUID.randomUUID(),
                    UUID.randomUUID()));
      }
      session.execute(batch);
    }
    for (int i = 0; i < 100; i++) {
      session.execute(String.format("INSERT INTO test_prepare (k, v) VALUES ('%d', %d)", i, i));
    }
  }

  @DataProvider(format = "%m[%p[0]]")
  public static Object[][] pagingOptions() {
    return new Object[][] {
      // exact # of rows.
      {new Options(100, false, 0, 0, 100, 1)},
      // # of rows - 1.
      {new Options(99, false, 0, 0, 100, 2)},
      // # of rows / 2.
      {new Options(50, false, 0, 0, 100, 2)},
      // # 1 row per page.
      {new Options(1, false, 0, 0, 100, 100)},
      // 10 rows per page, 10 pages overall = 100 (exact).
      {new Options(10, false, 10, 0, 100, 10)},
      // 10 rows per page, 9 pages overall = 90 (less than exact number of pages).
      {new Options(10, false, 9, 0, 90, 9)},
      // 10 rows per page, 2 pages per second should take ~5secs.
      {new Options(10, false, 0, 2, 100, 10)},
      // 8 bytes per page == 1 row per page as len(4) + int(4) for each row.
      {new Options(8, true, 0, 0, 100, 100)},
      // 16 bytes per page == 2 rows page per page.
      {new Options(16, true, 0, 0, 100, 50)},
      // 32 bytes per page == 4 rows per page.
      {new Options(32, true, 0, 0, 100, 25)}
    };
  }

  protected void validateMetrics(DseSession session) {
    Node node = session.getMetadata().getNodes().values().iterator().next();
    assertThat(session.getMetrics()).isPresent();
    Metrics metrics = session.getMetrics().get();
    assertThat(metrics.getNodeMetric(node, DefaultNodeMetric.CQL_MESSAGES)).isPresent();
    Timer messages = (Timer) metrics.getNodeMetric(node, DefaultNodeMetric.CQL_MESSAGES).get();
    assertThat(messages.getCount()).isGreaterThan(0);
    assertThat(messages.getMeanRate()).isGreaterThan(0);
    assertThat(metrics.getSessionMetric(DseSessionMetric.CONTINUOUS_CQL_REQUESTS)).isPresent();
    Timer requests =
        (Timer) metrics.getSessionMetric(DseSessionMetric.CONTINUOUS_CQL_REQUESTS).get();
    assertThat(requests.getCount()).isGreaterThan(0);
    assertThat(requests.getMeanRate()).isGreaterThan(0);
  }

  public static class Options {
    public int pageSize;
    public boolean sizeInBytes;
    public int maxPages;
    public int maxPagesPerSecond;
    public int expectedRows;
    public int expectedPages;

    Options(
        int pageSize,
        boolean sizeInBytes,
        int maxPages,
        int maxPagesPerSecond,
        int expectedRows,
        int expectedPages) {
      this.pageSize = pageSize;
      this.sizeInBytes = sizeInBytes;
      this.maxPages = maxPages;
      this.maxPagesPerSecond = maxPagesPerSecond;
      this.expectedRows = expectedRows;
      this.expectedPages = expectedPages;
    }

    public DriverExecutionProfile asProfile(DseSession session) {
      return session
          .getContext()
          .getConfig()
          .getDefaultProfile()
          .withInt(DseDriverOption.CONTINUOUS_PAGING_PAGE_SIZE, pageSize)
          .withBoolean(DseDriverOption.CONTINUOUS_PAGING_PAGE_SIZE_BYTES, sizeInBytes)
          .withInt(DseDriverOption.CONTINUOUS_PAGING_MAX_PAGES, maxPages)
          .withInt(DseDriverOption.CONTINUOUS_PAGING_MAX_PAGES_PER_SECOND, maxPagesPerSecond);
    }

    @Override
    public String toString() {
      return "pageSize="
          + pageSize
          + ", sizeInBytes="
          + sizeInBytes
          + ", maxPages="
          + maxPages
          + ", maxPagesPerSecond="
          + maxPagesPerSecond
          + ", expectedRows="
          + expectedRows
          + ", expectedPages="
          + expectedPages;
    }
  }
}
