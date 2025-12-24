package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.search.ILogHandler;
import codes.shubham.mvtscli.search.ILogSearchHandler;
import codes.shubham.mvtscli.source.LogLine;

public class IndexHandler implements ILogSearchHandler {
  private final Indexer indexer;

  public IndexHandler(Indexer indexer) {
    this.indexer = indexer;
  }


  @Override
  public void handle(LogLine logline) {
    indexer.index(logline);
  }

  @Override
  public boolean isFound() {
    return false;
  }
}
