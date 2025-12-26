package codes.shubham.mvtscli.cli.handlers;

import codes.shubham.mvtscli.cli.source.LogLine;

public interface ILogHandler {
  public void handle(LogLine logline);
  public default void commit(){};
  public default void init(){};
}
