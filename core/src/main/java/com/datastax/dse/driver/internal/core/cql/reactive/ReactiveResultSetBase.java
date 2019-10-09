/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.internal.core.cql.reactive;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet;
import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.AsyncPagingIterable;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.api.core.cql.Row;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import net.jcip.annotations.ThreadSafe;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

@ThreadSafe
public abstract class ReactiveResultSetBase<ResultSetT extends AsyncPagingIterable<Row, ResultSetT>>
    implements ReactiveResultSet {

  private final Callable<CompletionStage<ResultSetT>> firstPage;

  private final AtomicBoolean once = new AtomicBoolean(false);

  private final SimpleUnicastProcessor<ColumnDefinitions> columnDefinitionsPublisher =
      new SimpleUnicastProcessor<>();

  private final SimpleUnicastProcessor<ExecutionInfo> executionInfosPublisher =
      new SimpleUnicastProcessor<>();

  private final SimpleUnicastProcessor<Boolean> wasAppliedPublisher =
      new SimpleUnicastProcessor<>();

  protected ReactiveResultSetBase(Callable<CompletionStage<ResultSetT>> firstPage) {
    this.firstPage = firstPage;
  }

  @Override
  public void subscribe(@NonNull Subscriber<? super ReactiveRow> subscriber) {
    // As per rule 1.9, we need to throw an NPE if subscriber is null
    Objects.requireNonNull(subscriber, "Subscriber cannot be null");
    // As per rule 1.11, this publisher is allowed to support only one subscriber.
    if (once.compareAndSet(false, true)) {
      ReactiveResultSetSubscription<ResultSetT> subscription =
          new ReactiveResultSetSubscription<>(
              subscriber, columnDefinitionsPublisher, executionInfosPublisher, wasAppliedPublisher);
      try {
        subscriber.onSubscribe(subscription);
        // must be done after onSubscribe
        subscription.start(firstPage);
      } catch (Throwable t) {
        // As per rule 2.13: In the case that this rule is violated,
        // any associated Subscription to the Subscriber MUST be considered as
        // cancelled, and the caller MUST raise this error condition in a fashion
        // that is adequate for the runtime environment.
        subscription.doOnError(
            new IllegalStateException(
                subscriber
                    + " violated the Reactive Streams rule 2.13 by throwing an exception from onSubscribe.",
                t));
      }
    } else {
      subscriber.onSubscribe(EmptySubscription.INSTANCE);
      subscriber.onError(
          new IllegalStateException("This publisher does not support multiple subscriptions"));
    }
    // As per 2.13, this method must return normally (i.e. not throw)
  }

  @NonNull
  @Override
  public Publisher<? extends ColumnDefinitions> getColumnDefinitions() {
    return columnDefinitionsPublisher;
  }

  @NonNull
  @Override
  public Publisher<? extends ExecutionInfo> getExecutionInfos() {
    return executionInfosPublisher;
  }

  @NonNull
  @Override
  public Publisher<Boolean> wasApplied() {
    return wasAppliedPublisher;
  }
}
