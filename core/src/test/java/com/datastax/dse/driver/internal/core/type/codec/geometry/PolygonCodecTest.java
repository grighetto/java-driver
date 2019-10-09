/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.internal.core.type.codec.geometry;

import com.datastax.dse.driver.api.core.data.geometry.Polygon;
import com.datastax.dse.driver.internal.core.data.geometry.DefaultPoint;
import com.datastax.dse.driver.internal.core.data.geometry.DefaultPolygon;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(DataProviderRunner.class)
public class PolygonCodecTest extends GeometryCodecTest<Polygon, PolygonCodec> {
  private static Polygon polygon =
      new DefaultPolygon(
          new DefaultPoint(30, 10),
          new DefaultPoint(10, 20),
          new DefaultPoint(20, 40),
          new DefaultPoint(40, 40));

  public PolygonCodecTest() {
    super(new PolygonCodec());
  }

  @DataProvider
  public static Object[][] serde() {
    return new Object[][] {{null, null}, {polygon, polygon}};
  }

  @DataProvider
  public static Object[][] format() {
    return new Object[][] {
      {null, "NULL"}, {polygon, "'POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))'"}
    };
  }

  @DataProvider
  public static Object[][] parse() {
    return new Object[][] {
      {null, null},
      {"", null},
      {" ", null},
      {"NULL", null},
      {" NULL ", null},
      {"'POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))'", polygon},
      {" ' Polygon ( ( 30 10, 40 40, 20 40, 10 20, 30 10 ) ) ' ", polygon}
    };
  }

  @Test
  @UseDataProvider("format")
  @Override
  public void should_format(Polygon input, String expected) {
    super.should_format(input, expected);
  }

  @Test
  @UseDataProvider("parse")
  @Override
  public void should_parse(String input, Polygon expected) {
    super.should_parse(input, expected);
  }
}
