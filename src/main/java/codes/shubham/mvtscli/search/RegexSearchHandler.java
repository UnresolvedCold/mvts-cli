package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.LogLine;

public class RegexSearchHandler implements  ILogHandler {

  private final String searchTerm;

  public RegexSearchHandler(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  @Override
  public void handle(LogLine logLine) {
    if (logLine.line() == null || logLine.line().isEmpty()) return;

    if (logLine.line().matches(".*" + searchTerm + ".*")) {
      synchronized (System.out) {
        System.out.println(logLine.line());
      }
    }
  }
}
