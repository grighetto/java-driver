/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.graph;

import com.datastax.oss.driver.api.core.DefaultProtocolVersion;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.api.core.specex.SpeculativeExecutionPolicy;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

/** Information about the execution of a graph statement. */
public interface GraphExecutionInfo {

  /** The statement that was executed. */
  GraphStatement<?> getStatement();

  /** The node that was used as a coordinator to successfully complete the query. */
  Node getCoordinator();

  /**
   * The number of speculative executions that were started for this query.
   *
   * <p>This does not include the initial, normal execution of the query. Therefore, if speculative
   * executions are disabled, this will always be 0. If they are enabled and one speculative
   * execution was triggered in addition to the initial execution, this will be 1, etc.
   *
   * @see SpeculativeExecutionPolicy
   */
  int getSpeculativeExecutionCount();

  /**
   * The index of the execution that completed this query.
   *
   * <p>0 represents the initial, normal execution of the query, 1 the first speculative execution,
   * etc.
   *
   * @see SpeculativeExecutionPolicy
   */
  int getSuccessfulExecutionIndex();

  /**
   * The errors encountered on previous coordinators, if any.
   *
   * <p>The list is in chronological order, based on the time that the driver processed the error
   * responses. If speculative executions are enabled, they run concurrently so their errors will be
   * interleaved. A node can appear multiple times (if the retry policy decided to retry on the same
   * node).
   */
  List<Map.Entry<Node, Throwable>> getErrors();

  /**
   * The server-side warnings for this query, if any (otherwise the list will be empty).
   *
   * <p>This feature is only available with {@link DefaultProtocolVersion#V4} or above; with lower
   * versions, this list will always be empty.
   */
  List<String> getWarnings();

  /**
   * The custom payload sent back by the server with the response, if any (otherwise the map will be
   * empty).
   *
   * <p>This method returns a read-only view of the original map, but its values remain inherently
   * mutable. If multiple clients will read these values, care should be taken not to corrupt the
   * data (in particular, preserve the indices by calling {@link ByteBuffer#duplicate()}).
   *
   * <p>This feature is only available with {@link DefaultProtocolVersion#V4} or above; with lower
   * versions, this map will always be empty.
   */
  Map<String, ByteBuffer> getIncomingPayload();
}
