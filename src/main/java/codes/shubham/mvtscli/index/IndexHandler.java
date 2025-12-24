package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.search.ILogHandler;
import codes.shubham.mvtscli.source.LogLine;

public class IndexHandler implements ILogHandler {
  private final Indexer indexer;

  public IndexHandler(Indexer indexer) {
    this.indexer = indexer;
  }


  @Override
  public void handle(LogLine logline) {
    indexer.index(logline);
  }
}
