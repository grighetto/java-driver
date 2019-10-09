/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.type.codec;

import com.datastax.dse.driver.api.core.data.geometry.LineString;
import com.datastax.dse.driver.api.core.data.geometry.Point;
import com.datastax.dse.driver.api.core.data.geometry.Polygon;
import com.datastax.dse.driver.api.core.data.time.DateRange;
import com.datastax.dse.driver.internal.core.type.codec.geometry.LineStringCodec;
import com.datastax.dse.driver.internal.core.type.codec.geometry.PointCodec;
import com.datastax.dse.driver.internal.core.type.codec.geometry.PolygonCodec;
import com.datastax.dse.driver.internal.core.type.codec.time.DateRangeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodec;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;

/** Extends {@link TypeCodecs} to handle DSE-specific types. */
public class DseTypeCodecs extends TypeCodecs {

  public static final TypeCodec<LineString> LINE_STRING = new LineStringCodec();

  public static final TypeCodec<Point> POINT = new PointCodec();

  public static final TypeCodec<Polygon> POLYGON = new PolygonCodec();

  public static final TypeCodec<DateRange> DATE_RANGE = new DateRangeCodec();
}
