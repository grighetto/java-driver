/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.cql.reactive;

import com.datastax.dse.driver.internal.core.cql.reactive.CqlRequestReactiveProcessor;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.session.Session;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import org.reactivestreams.Publisher;

/**
 * A {@link Session} that offers utility methods to issue queries using reactive-style programming.
 *
 * <p>Methods in this interface all return {@link ReactiveResultSet} instances. See the javadocs of
 * this interface for important remarks anc caveats regarding the subscription to and consumption of
 * reactive result sets.
 *
 * @see ReactiveResultSet
 * @see ReactiveRow
 */
public interface ReactiveSession extends Session {

  /**
   * Returns a {@link Publisher} that, once subscribed to, executes the given query and emits all
   * the results.
   *
   * @param query the query to execute.
   * @return The {@link Publisher} that will publish the returned results.
   */
  @NonNull
  default ReactiveResultSet executeReactive(@NonNull String query) {
    return executeReactive(SimpleStatement.newInstance(query));
  }

  /**
   * Returns a {@link Publisher} that, once subscribed to, executes the given query and emits all
   * the results.
   *
   * @param statement the statement to execute.
   * @return The {@link Publisher} that will publish the returned results.
   */
  @NonNull
  default ReactiveResultSet executeReactive(@NonNull Statement<?> statement) {
    return Objects.requireNonNull(
        execute(statement, CqlRequestReactiveProcessor.REACTIVE_RESULT_SET));
  }
}
