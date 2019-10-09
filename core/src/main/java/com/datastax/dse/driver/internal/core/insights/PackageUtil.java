/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.internal.core.insights;

import com.datastax.oss.driver.shaded.guava.common.annotations.VisibleForTesting;
import com.datastax.oss.driver.shaded.guava.common.base.Joiner;
import java.util.Arrays;
import java.util.regex.Pattern;

class PackageUtil {
  static final String DEFAULT_SPECULATIVE_EXECUTION_PACKAGE =
      "com.datastax.oss.driver.internal.core.specex";
  static final String DEFAULT_LOAD_BALANCING_PACKAGE =
      "com.datastax.oss.driver.internal.core.loadbalancing";
  static final String DEFAULT_AUTH_PROVIDER_PACKAGE = "com.datastax.oss.driver.internal.core.auth";
  private static final Pattern PACKAGE_SPLIT_REGEX = Pattern.compile("\\.");
  private static final Joiner DOT_JOINER = Joiner.on(".");

  static String getNamespace(Class<?> tClass) {
    String namespace = "";
    Package packageInfo = tClass.getPackage();
    if (packageInfo != null) {
      namespace = packageInfo.getName();
    }
    return namespace;
  }

  static ClassSettingDetails getSpeculativeExecutionDetails(String classSetting) {
    return getClassSettingDetails(classSetting, DEFAULT_SPECULATIVE_EXECUTION_PACKAGE);
  }

  static ClassSettingDetails getLoadBalancingDetails(String classSetting) {
    return getClassSettingDetails(classSetting, DEFAULT_LOAD_BALANCING_PACKAGE);
  }

  static ClassSettingDetails getAuthProviderDetails(String classSetting) {
    return getClassSettingDetails(classSetting, DEFAULT_AUTH_PROVIDER_PACKAGE);
  }

  private static ClassSettingDetails getClassSettingDetails(
      String classSetting, String packageName) {
    String className = getClassName(classSetting);
    String fullPackage = getFullPackageOrDefault(classSetting, packageName);
    return new ClassSettingDetails(className, fullPackage);
  }

  @VisibleForTesting
  static String getClassName(String classSetting) {
    String[] split = PACKAGE_SPLIT_REGEX.split(classSetting);
    if (split.length == 0) {
      return "";
    }
    return split[split.length - 1];
  }

  @VisibleForTesting
  static String getFullPackageOrDefault(String classSetting, String defaultValue) {
    String[] split = PACKAGE_SPLIT_REGEX.split(classSetting);
    if (split.length <= 1) return defaultValue;
    return DOT_JOINER.join(Arrays.copyOf(split, split.length - 1));
  }

  static class ClassSettingDetails {
    private final String className;
    private final String fullPackage;

    ClassSettingDetails(String className, String fullPackage) {
      this.className = className;
      this.fullPackage = fullPackage;
    }

    String getClassName() {
      return className;
    }

    String getFullPackage() {
      return fullPackage;
    }
  }
}
