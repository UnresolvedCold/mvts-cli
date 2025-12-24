package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;

public class RegexSearcher implements  ILogSearcher {

  @Override
  public void search(ILogSource source, IOSearchMode mode,
                     String searchTerm, ILogHandler matchHandler, ILogHandler lineHandler) {
    String finalSearchTerm = ".*" + searchTerm + ".*";

    try (source) {
      long[] lineNo = {0};

      source.lines().forEach(line -> {
        lineNo[0]++;
        if (line == null || line.isEmpty()) return;
        lineHandler.handle(line, lineNo[0]);

        if (line.matches(finalSearchTerm)) {
          matchHandler.handle(line, lineNo[0]);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
