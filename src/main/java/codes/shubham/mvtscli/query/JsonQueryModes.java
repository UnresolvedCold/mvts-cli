package codes.shubham.mvtscli.query;

import java.util.HashSet;
import java.util.Set;

public enum JsonQueryModes {
  task("task_list", new HashSet<String>(Set.of("t")));

  private final String entity;
  private final Set<String> aliases;


  JsonQueryModes(String entity, Set<String> aliases) {
    this.entity = entity;
    this.aliases = aliases;
  }
}
