package codes.shubham.mvtscli.handlers;

import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.source.LogLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexHandler implements ILogSearchHandler {
  private static final Logger logger = LoggerFactory.getLogger(IndexHandler.class.getName());
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

  @Override
  public void commit() {
    indexer.commit();
  }

  @Override
  public void init() {
  }
}
