package codes.shubham.mvtscli.handlers;

import codes.shubham.mvtscli.source.LogLine;

public interface ILogHandler {
  public void handle(LogLine logline);
  public default void commit(){};
  public default void init(){};
}
