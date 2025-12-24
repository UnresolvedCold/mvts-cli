package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.search.ILogSearchHandler;
import codes.shubham.mvtscli.source.LogLine;

public class IndexValidateHandler implements ILogSearchHandler {
  private final Indexer indexer;
  private final String requestID;

  public IndexValidateHandler(Indexer indexer, String requestID) {
    this.indexer = indexer;
    this.requestID = requestID;
  }

  @Override
  public boolean isFound() {
    return false;
  }

  @Override
  public void handle(LogLine logline) {
    if (requestID == null || requestID.trim().isBlank()) return;
    indexer.validate(logline);
  }
}
