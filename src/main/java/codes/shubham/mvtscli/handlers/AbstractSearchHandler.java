package codes.shubham.mvtscli.handlers;

import codes.shubham.mvtscli.ApplicationProperties;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.source.LogLine;

public abstract class AbstractSearchHandler implements ILogSearchHandler {

  private final String requestID;
  private final String searchTerm;
  private final ILogHandler indexHandler;

  public AbstractSearchHandler(String requestID, String searchTerm) {
    this.searchTerm = searchTerm;
    this.requestID = requestID;
    indexHandler = new IndexHandler(new Indexer(ApplicationProperties.RECENT_SEARCH_INDEX_FILE.getValue()));
  }

  @Override
  public void handle(LogLine logLine) {
    if (logLine.line() == null || logLine.line().isEmpty() || isFound()) return;

    if (logLine.line().matches(searchTerm)) {
      internalHandle(logLine);
    }

    String sub = logLine.line().substring(0, Math.min(logLine.line().length(), 300));

    if (sub.matches(".*"+requestID+".*")) {
      indexHandler.handle(logLine);
    }
  }

  protected abstract void internalHandle(LogLine logLine);

  @Override
  public void commit() {
    indexHandler.commit();
  }
}
