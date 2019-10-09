/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.internal.core.insights.schema;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.Objects;

public class ReconnectionPolicyInfo {
  @JsonProperty("type")
  private final String type;

  @JsonProperty("options")
  private final Map<String, Object> options;

  @JsonProperty("namespace")
  private final String namespace;

  @JsonCreator
  public ReconnectionPolicyInfo(
      @JsonProperty("type") String type,
      @JsonProperty("options") Map<String, Object> options,
      @JsonProperty("namespace") String namespace) {

    this.type = type;
    this.options = options;
    this.namespace = namespace;
  }

  public String getType() {
    return type;
  }

  public Map<String, Object> getOptions() {
    return options;
  }

  public String getNamespace() {
    return namespace;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReconnectionPolicyInfo)) {
      return false;
    }
    ReconnectionPolicyInfo that = (ReconnectionPolicyInfo) o;
    return Objects.equals(type, that.type)
        && Objects.equals(options, that.options)
        && Objects.equals(namespace, that.namespace);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, options, namespace);
  }

  @Override
  public String toString() {
    return "ReconnectionPolicyInfo{"
        + "type='"
        + type
        + '\''
        + ", options="
        + options
        + ", namespace='"
        + namespace
        + '\''
        + '}';
  }
}
