package codes.shubham.mvtscli.cli.search;

import codes.shubham.mvtscli.cli.handlers.ILogHandler;
import codes.shubham.mvtscli.cli.source.ILogSource;
import codes.shubham.mvtscli.cli.source.LogLine;

import java.util.List;
import java.util.stream.Stream;

/***
 * Run through logs line by line and handle each line via the provided handler
 ***/
public final class LogRunner {

  public LogRunner() {
  }

  public void run(ILogSource source, List<ILogHandler> handlerList) {
    try (source) {
      handlerList.forEach(ILogHandler::init);

      Stream<LogLine> lines = source.logLines();
      Iterable<LogLine> iter = (Iterable<LogLine>) lines::iterator;

      for (LogLine logLine : iter) {
        if (logLine.line() == null || logLine.line().isBlank()) continue;

        handle(handlerList, logLine);
      }

      handlerList.forEach(ILogHandler::commit);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static void handle(List<ILogHandler> handlerList, LogLine logLine) {
    if (handlerList != null && !handlerList.isEmpty()) {
      for (ILogHandler handler : handlerList) {
        handler.handle(logLine);
      }
    }
  }
}
