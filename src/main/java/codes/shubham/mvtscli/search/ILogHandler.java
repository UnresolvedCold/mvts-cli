package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.LogLine;

public interface ILogHandler {
  public void handle(LogLine logline);
}
