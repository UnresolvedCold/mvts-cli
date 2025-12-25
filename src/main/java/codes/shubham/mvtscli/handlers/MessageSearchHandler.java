package codes.shubham.mvtscli.handlers;

import codes.shubham.mvtscli.source.LogLine;

public class MessageSearchHandler extends AbstractSearchHandler {
  boolean found = false;
  String marker;

  public MessageSearchHandler(String requestID, String marker) {
    super(requestID,".*"+ marker +".*"+requestID+".*");
    this.marker = marker;
  }

  @Override
  public boolean isFound() {
    return found;
  }

  @Override
  protected void internalHandle(LogLine logLine) {

    synchronized (System.out) {
      found = true;
      System.out.println(logLine.line().split(marker)[1].trim());
    }
  }
}
