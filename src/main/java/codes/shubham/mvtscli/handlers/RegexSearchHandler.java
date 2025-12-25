package codes.shubham.mvtscli.handlers;

import codes.shubham.mvtscli.handlers.searchresult.ISearchedResultHandler;
import codes.shubham.mvtscli.source.LogLine;

public class RegexSearchHandler extends AbstractSearchHandler {
  private final ISearchedResultHandler handler;


  public RegexSearchHandler(String requestID, String searchTerm, ISearchedResultHandler handler) {
    super(requestID,".*" + requestID + ".*" + searchTerm + ".*");
    this.handler = handler;
  }

  @Override
  public boolean isFound() {
    return false;
  }

  @Override
  protected void internalHandle(LogLine logLine) {
    if (handler == null) return;

    handler.handle(logLine.line());
  }
}
