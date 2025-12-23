package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;

import java.util.ArrayList;
import java.util.List;

public class RegexSearcher implements  ILogSearcher {
  @Override
  public List<String> search(ILogSource source, IOSearchMode mode, String searchTerm) {
    String finalSearchTerm = ".*" + searchTerm + ".*";
    List<String> results = new ArrayList<>();
    try (source) {
      source.lines().forEach(line -> {
        if (line == null || line.isEmpty()) return;
        if (line.matches(finalSearchTerm)) {
          results.add(line);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return results;
  }
}
