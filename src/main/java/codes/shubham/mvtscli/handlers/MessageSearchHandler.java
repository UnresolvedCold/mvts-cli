package codes.shubham.mvtscli.handlers;

import codes.shubham.mvtscli.handlers.searchresult.ISearchedResultHandler;
import codes.shubham.mvtscli.source.LogLine;

public class MessageSearchHandler extends AbstractSearchHandler {
  private final boolean found = false;
  private final String marker;
  private final ISearchedResultHandler handler;

  public MessageSearchHandler(String requestID, String marker, ISearchedResultHandler handler) {
    super(requestID,".*"+ marker +".*"+requestID+".*");
    this.marker = marker;
    this.handler = handler;
  }

  @Override
  public boolean isFound() {
    return found;
  }

  @Override
  protected void internalHandle(LogLine logLine) {
    if (handler == null) return;

    handler.handle(logLine.line());
  }
}
