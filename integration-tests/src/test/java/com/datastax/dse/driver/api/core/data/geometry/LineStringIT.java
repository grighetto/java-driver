/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.data.geometry;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.dse.driver.api.core.DseSession;
import com.datastax.dse.driver.api.testinfra.session.DseSessionRuleBuilder;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.uuid.Uuids;
import com.datastax.oss.driver.api.testinfra.DseRequirement;
import com.datastax.oss.driver.api.testinfra.ccm.CcmRule;
import com.datastax.oss.driver.api.testinfra.session.SessionRule;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

@DseRequirement(min = "5.0")
public class LineStringIT extends GeometryIT<LineString> {

  private static CcmRule ccm = CcmRule.getInstance();

  private static SessionRule<DseSession> sessionRule = new DseSessionRuleBuilder(ccm).build();

  @ClassRule public static TestRule chain = RuleChain.outerRule(ccm).around(sessionRule);

  private static final String LINE_STRING_TYPE = "LineStringType";

  public LineStringIT() {
    super(
        Lists.newArrayList(
            LineString.fromPoints(Point.fromCoordinates(0, 10), Point.fromCoordinates(10, 0)),
            LineString.fromPoints(
                Point.fromCoordinates(30, 10),
                Point.fromCoordinates(10, 30),
                Point.fromCoordinates(40, 40)),
            LineString.fromPoints(
                Point.fromCoordinates(-5, 0),
                Point.fromCoordinates(0, 10),
                Point.fromCoordinates(10, 5))),
        LineString.class,
        sessionRule);
  }

  @BeforeClass
  public static void initialize() {
    onTestContextInitialized(LINE_STRING_TYPE, sessionRule);
  }

  @Test
  public void should_insert_and_retrieve_empty_linestring() {
    LineString empty = LineString.fromWellKnownText("LINESTRING EMPTY");
    UUID key = Uuids.random();
    sessionRule
        .session()
        .execute(
            SimpleStatement.builder("INSERT INTO tbl (k, g) VALUES (?, ?)")
                .addPositionalValues(key, empty)
                .build());

    ResultSet result =
        sessionRule
            .session()
            .execute(
                SimpleStatement.builder("SELECT g from tbl where k=?")
                    .addPositionalValues(key)
                    .build());
    Row row = result.iterator().next();
    List<Point> points = row.get("g", LineString.class).getPoints();
    assertThat(points.isEmpty()).isTrue();
  }
}
