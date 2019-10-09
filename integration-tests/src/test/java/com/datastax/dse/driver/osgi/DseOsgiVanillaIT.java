/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.osgi;

import static com.datastax.dse.driver.osgi.support.DseBundleOptions.baseOptions;
import static com.datastax.dse.driver.osgi.support.DseBundleOptions.driverCoreBundle;
import static com.datastax.dse.driver.osgi.support.DseBundleOptions.driverDseBundle;
import static com.datastax.dse.driver.osgi.support.DseBundleOptions.driverDseQueryBuilderBundle;
import static com.datastax.dse.driver.osgi.support.DseBundleOptions.driverQueryBuilderBundle;
import static com.datastax.oss.driver.osgi.BundleOptions.jacksonBundles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.ops4j.pax.exam.CoreOptions.options;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.datastax.dse.driver.osgi.support.DseOsgiSimpleTests;
import com.datastax.oss.driver.api.testinfra.ccm.CustomCcmRule;
import com.datastax.oss.driver.categories.IsolatedTests;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
@Category(IsolatedTests.class)
public class DseOsgiVanillaIT implements DseOsgiSimpleTests {

  @ClassRule
  public static final CustomCcmRule CCM_RULE = CustomCcmRule.builder().withNodes(1).build();

  @Configuration
  public Option[] config() {
    // this configuration purposely excludes bundles whose resolution is optional:
    // ESRI, Reactive Streams and Tinkerpop. This allows to validate that the driver can still
    // work properly in an OSGi container as long as the missing packages are not accessed.
    return options(
        driverDseBundle(),
        driverDseQueryBuilderBundle(),
        driverCoreBundle(),
        driverQueryBuilderBundle(),
        baseOptions(),
        jacksonBundles());
  }

  @Before
  public void addTestAppender() {
    Logger logger = (Logger) LoggerFactory.getLogger("com.datastax.dse.driver");
    Level oldLevel = logger.getLevel();
    logger.getLoggerContext().putObject("oldLevel", oldLevel);
    logger.setLevel(Level.WARN);
    TestAppender appender = new TestAppender();
    logger.addAppender(appender);
    appender.start();
  }

  @After
  public void removeTestAppender() {
    Logger logger = (Logger) LoggerFactory.getLogger("com.datastax.dse.driver");
    logger.detachAppender("test");
    Level oldLevel = (Level) logger.getLoggerContext().getObject("oldLevel");
    logger.setLevel(oldLevel);
  }

  @Test
  public void should_connect_and_query_simple() {
    connectAndQuerySimple();
    assertLogMessagesPresent();
  }

  private void assertLogMessagesPresent() {
    Logger logger = (Logger) LoggerFactory.getLogger("com.datastax.dse.driver");
    TestAppender appender = (TestAppender) logger.getAppender("test");
    List<String> warnLogs =
        appender.events.stream()
            .filter(event -> event.getLevel().toInt() >= Level.WARN.toInt())
            .map(ILoggingEvent::getFormattedMessage)
            .collect(Collectors.toList());
    assertThat(warnLogs).hasSize(3);
    assertThat(warnLogs)
        .anySatisfy(
            msg ->
                assertThat(msg)
                    .contains(
                        "Could not register Geo codecs; ESRI API might be missing from classpath"))
        .anySatisfy(
            msg ->
                assertThat(msg)
                    .contains(
                        "Could not register Reactive extensions; Reactive Streams API might be missing from classpath"))
        .anySatisfy(
            msg ->
                assertThat(msg)
                    .contains(
                        "Could not register Graph extensions; Tinkerpop API might be missing from classpath"));
  }

  private static class TestAppender extends AppenderBase<ILoggingEvent> {

    private final List<ILoggingEvent> events = new CopyOnWriteArrayList<>();

    private TestAppender() {
      name = "test";
    }

    @Override
    protected void append(ILoggingEvent event) {
      events.add(event);
    }
  }
}
