package com.datastax.driver.core;

import com.datastax.driver.core.ColumnMetadata.IndexMetadata;

/**
 * Augment AssertJ with custom assertions for the Java driver.
 */
public class Assertions extends org.assertj.core.api.Assertions{
    public static ClusterAssert assertThat(Cluster cluster) {
        return new ClusterAssert(cluster);
    }

    public static SessionAssert assertThat(Session session) {
        return new SessionAssert(session);
    }

    public static TokenRangeAssert assertThat(TokenRange range) {
        return new TokenRangeAssert(range);
    }

    public static DataTypeAssert assertThat(DataType type) {
        return new DataTypeAssert(type);
    }
    
    public static IndexMetadataAssert assertThat(IndexMetadata indexMetadata) {
        return new IndexMetadataAssert(indexMetadata);
    }
}
