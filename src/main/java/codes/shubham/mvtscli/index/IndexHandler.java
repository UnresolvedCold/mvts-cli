package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.search.ILogHandler;

public class IndexHandler implements ILogHandler {
  private final Indexer indexer;

  public IndexHandler(Indexer indexer) {
    this.indexer = indexer;
  }


  @Override
  public void handle(String line, long lineNumber, String filePath) {
    indexer.index(line, filePath, lineNumber);
  }
}
