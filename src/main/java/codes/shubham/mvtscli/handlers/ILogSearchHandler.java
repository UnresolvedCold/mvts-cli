package codes.shubham.mvtscli.handlers;

import codes.shubham.mvtscli.source.LogLine;

public interface ILogSearchHandler extends ILogHandler {
  public boolean isFound();
}
