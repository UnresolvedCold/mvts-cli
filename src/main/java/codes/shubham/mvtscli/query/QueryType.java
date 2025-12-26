package codes.shubham.mvtscli.query;

import java.util.HashSet;
import java.util.Set;

public enum QueryType {
  JMESPATH("jmespath", new HashSet<>(Set.of("jmespath", "jp", "j"))),
  RECIPIE("recipie", new HashSet<>(Set.of("recipie", "rcp", "r")));

  private final String typeName;
  private final Set<String> aliases;

  QueryType(String typeName, Set<String> aliases) {
    this.typeName = typeName;
    this.aliases = aliases;
  }

  public String getTypeName() {
    return typeName;
  }

  public Set<String> getAliases() {
    return aliases;
  }

  public static QueryType fromString(String input) {
    for (QueryType qt : QueryType.values()) {
      if (qt.getAliases().contains(input.toLowerCase())) {
        return qt;
      }
    }
    throw new IllegalArgumentException("Unknown QueryType: " + input);
  }
}
