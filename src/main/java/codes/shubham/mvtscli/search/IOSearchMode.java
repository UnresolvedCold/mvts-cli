package codes.shubham.mvtscli.search;

import java.util.HashSet;
import java.util.Set;

public enum IOSearchMode {
  REGEX("regex", "", Set.of("r")),
  MESSAGE("message", " Message: ", Set.of("m")),
  OUTPUT("output", " Output: ", Set.of("o"));

  String key;
  String marker;
  Set<String> aliases;

  IOSearchMode(String key, String marker, Set<String> aliases) {
    this.key = key;
    this.marker = marker;
    this.aliases = aliases;
  }

  public static IOSearchMode getMode(String stringMode) {
    for (IOSearchMode mode : IOSearchMode.values()) {
      if (mode.key.equalsIgnoreCase(stringMode)
          || mode.aliases.contains(stringMode)) {
        return mode;
      }
    }
    throw new IllegalArgumentException("Invalid search mode: " + stringMode);
  }

  public String marker() {
    return marker;
  }
}
