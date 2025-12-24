package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.LogLine;

public class RegexSearchHandler extends AbstractSearchHandler {
  public RegexSearchHandler(String searchTerm) {
    super(".*" + searchTerm + ".*");
  }

  @Override
  public boolean isFound() {
    return false;
  }

  @Override
  protected void internalHandle(LogLine logLine) {
    synchronized (System.out) {
      System.out.println(logLine.line());
    }
  }
}
