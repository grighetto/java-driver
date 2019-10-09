/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.internal.core.cql.continuous;

import static com.datastax.oss.driver.Assertions.assertThatStage;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

import com.datastax.dse.driver.DseTestDataProviders;
import com.datastax.dse.driver.DseTestFixtures;
import com.datastax.dse.driver.api.core.DseProtocolVersion;
import com.datastax.dse.driver.api.core.cql.continuous.ContinuousAsyncResultSet;
import com.datastax.oss.driver.api.core.NoNodeAvailableException;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.session.Request;
import com.datastax.oss.driver.api.core.session.Session;
import com.datastax.oss.driver.internal.core.cql.RequestHandlerTestHarness;
import com.datastax.oss.driver.internal.core.metadata.LoadBalancingPolicyWrapper;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.util.concurrent.CompletionStage;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

public class ContinuousCqlRequestHandlerNodeTargetingTest
    extends ContinuousCqlRequestHandlerTestBase {

  @Test
  @UseDataProvider(value = "allDseProtocolVersions", location = DseTestDataProviders.class)
  public void should_fail_if_targeted_node_not_available(DseProtocolVersion version) {
    try (RequestHandlerTestHarness harness =
        continuousHarnessBuilder()
            .withResponse(node1, defaultFrameOf(DseTestFixtures.singleDseRow()))
            .withResponse(node2, defaultFrameOf(DseTestFixtures.singleDseRow()))
            .withEmptyPool(node3)
            .withProtocolVersion(version)
            .build()) {

      LoadBalancingPolicyWrapper loadBalancingPolicy =
          harness.getContext().getLoadBalancingPolicyWrapper();
      InOrder invocations = Mockito.inOrder(loadBalancingPolicy);

      // target node3, which should be unavailable
      CompletionStage<ContinuousAsyncResultSet> resultSetFuture =
          new ContinuousCqlRequestHandler(
                  UNDEFINED_IDEMPOTENCE_STATEMENT.setNode(node3),
                  harness.getSession(),
                  harness.getContext(),
                  "target node 3, unavailable")
              .handle();

      assertThatStage(resultSetFuture)
          .isFailed(
              error -> {
                assertThat(error).isInstanceOf(NoNodeAvailableException.class);
                invocations
                    .verify(loadBalancingPolicy, never())
                    .newQueryPlan(any(Request.class), anyString(), any(Session.class));
              });

      resultSetFuture =
          new ContinuousCqlRequestHandler(
                  UNDEFINED_IDEMPOTENCE_STATEMENT,
                  harness.getSession(),
                  harness.getContext(),
                  "no node targeting, should use node 1")
              .handle();

      assertThatStage(resultSetFuture)
          .isSuccess(
              resultSet -> {
                assertThat(resultSet.getExecutionInfo().getCoordinator()).isEqualTo(node1);
                invocations
                    .verify(loadBalancingPolicy)
                    .newQueryPlan(
                        UNDEFINED_IDEMPOTENCE_STATEMENT,
                        DriverExecutionProfile.DEFAULT_NAME,
                        harness.getSession());
              });

      resultSetFuture =
          new ContinuousCqlRequestHandler(
                  UNDEFINED_IDEMPOTENCE_STATEMENT,
                  harness.getSession(),
                  harness.getContext(),
                  "no node targeting, should use node 2")
              .handle();

      assertThatStage(resultSetFuture)
          .isSuccess(
              resultSet -> {
                assertThat(resultSet.getExecutionInfo().getCoordinator()).isEqualTo(node2);
                invocations
                    .verify(loadBalancingPolicy)
                    .newQueryPlan(
                        UNDEFINED_IDEMPOTENCE_STATEMENT,
                        DriverExecutionProfile.DEFAULT_NAME,
                        harness.getSession());
              });
    }
  }

  @Test
  @UseDataProvider(value = "allDseProtocolVersions", location = DseTestDataProviders.class)
  public void should_target_node(DseProtocolVersion version) {
    try (RequestHandlerTestHarness harness =
        continuousHarnessBuilder()
            .withResponse(node1, defaultFrameOf(DseTestFixtures.singleDseRow()))
            .withResponse(node2, defaultFrameOf(DseTestFixtures.singleDseRow()))
            .withResponse(node3, defaultFrameOf(DseTestFixtures.singleDseRow()))
            .withProtocolVersion(version)
            .build()) {

      LoadBalancingPolicyWrapper loadBalancingPolicy =
          harness.getContext().getLoadBalancingPolicyWrapper();
      InOrder invocations = Mockito.inOrder(loadBalancingPolicy);

      CompletionStage<ContinuousAsyncResultSet> resultSetFuture =
          new ContinuousCqlRequestHandler(
                  UNDEFINED_IDEMPOTENCE_STATEMENT.setNode(node3),
                  harness.getSession(),
                  harness.getContext(),
                  "target node 3")
              .handle();

      assertThatStage(resultSetFuture)
          .isSuccess(
              resultSet -> {
                assertThat(resultSet.getExecutionInfo().getCoordinator()).isEqualTo(node3);
                invocations
                    .verify(loadBalancingPolicy, never())
                    .newQueryPlan(any(Request.class), anyString(), any(Session.class));
              });

      resultSetFuture =
          new ContinuousCqlRequestHandler(
                  UNDEFINED_IDEMPOTENCE_STATEMENT,
                  harness.getSession(),
                  harness.getContext(),
                  "no node targeting")
              .handle();

      assertThatStage(resultSetFuture)
          .isSuccess(
              resultSet -> {
                assertThat(resultSet.getExecutionInfo().getCoordinator()).isEqualTo(node1);
                invocations
                    .verify(loadBalancingPolicy)
                    .newQueryPlan(
                        UNDEFINED_IDEMPOTENCE_STATEMENT,
                        DriverExecutionProfile.DEFAULT_NAME,
                        harness.getSession());
              });
    }
  }
}
