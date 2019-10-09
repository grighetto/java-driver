/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core;

import com.datastax.dse.protocol.internal.DseProtocolConstants;
import com.datastax.oss.driver.api.core.DefaultProtocolVersion;
import com.datastax.oss.driver.api.core.ProtocolVersion;

/**
 * A DSE-specific protocol version.
 *
 * <p>Legacy DSE versions did not have a specific version, but instead reused a Cassandra protocol
 * version: DSE 5.0 is supported via {@link DefaultProtocolVersion#V4}, and DSE 4.7 and 4.8 via
 * {@link DefaultProtocolVersion#V3}.
 *
 * <p>DSE 4.6 and earlier are not supported by this version of the driver, use the 1.x series.
 */
public enum DseProtocolVersion implements ProtocolVersion {

  /** Version 1, supported by DSE 5.1.0 and above. */
  DSE_V1(DseProtocolConstants.Version.DSE_V1, false),

  /** Version 2, supported by DSE 6 and above. */
  DSE_V2(DseProtocolConstants.Version.DSE_V2, false),
  ;

  private final int code;
  private final boolean beta;

  DseProtocolVersion(int code, boolean beta) {
    this.code = code;
    this.beta = beta;
  }

  @Override
  public int getCode() {
    return code;
  }

  @Override
  public boolean isBeta() {
    return beta;
  }
}
