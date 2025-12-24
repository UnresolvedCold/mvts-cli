package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.LogLine;

public class MessageSearchHandler extends AbstractSearchHandler {
  boolean found = false;

  public MessageSearchHandler(String requestID) {
    super(".*Message:.*"+requestID+".*");
  }

  @Override
  public boolean isFound() {
    return found;
  }

  @Override
  protected void internalHandle(LogLine logLine) {
    synchronized (System.out) {
      found = true;
      System.out.println(found);
    }
  }
}
