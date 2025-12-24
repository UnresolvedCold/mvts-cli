package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.LogLine;

public abstract class AbstractSearchHandler implements ILogSearchHandler {

  private final String searchTerm;

  public AbstractSearchHandler(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  @Override
  public void handle(LogLine logLine) {
    if (logLine.line() == null || logLine.line().isEmpty() || isFound()) return;

    if (logLine.line().matches(searchTerm)) {
      internalHandle(logLine);
    }
  }

  protected abstract void internalHandle(LogLine logLine);

}
