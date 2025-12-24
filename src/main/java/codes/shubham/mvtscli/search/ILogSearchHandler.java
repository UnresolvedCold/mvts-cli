package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.LogLine;

public interface ILogSearchHandler extends ILogHandler {
  public boolean isFound();
  public default void init(){}
  public default void onFirstLine(LogLine logLine){}
  public default void onLastLine(LogLine logLine){}
}
