package codes.shubham.mvtscli.helpers;

import codes.shubham.mvtscli.Main;

public class BuildInfo {
  private BuildInfo() {}

  public static String name() {
    return value("Implementation-Title", "unknown");
  }

  public static String version() {
    return value("Implementation-Version", "dev");
  }

  public static String vendor() {
    return value("Implementation-Vendor", "unknown");
  }

  private static String value(String key, String fallback) {
    Package pkg = Main.class.getPackage();
    if (pkg == null) return fallback;

    String value = switch (key) {
      case "Implementation-Title" -> pkg.getImplementationTitle();
      case "Implementation-Version" -> pkg.getImplementationVersion();
      case "Implementation-Vendor" -> pkg.getImplementationVendor();
      default -> null;
    };
    return value != null ? value : fallback;
  }
}
