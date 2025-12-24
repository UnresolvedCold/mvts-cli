package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;
import codes.shubham.mvtscli.source.LogLine;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

/***
 * Run through logs line by line and handle each line via the provided handler
 ***/
public final class LogRunner {
  private final AtomicBoolean terminateSearch;

  public LogRunner(AtomicBoolean terminateSearch) {
    this.terminateSearch = terminateSearch;
  }

  public void run(ILogSource source, List<ILogSearchHandler> handlerList) {
    try (source) {
      Stream<LogLine> lines = source.logLines();

      for (LogLine logLine : (Iterable<LogLine>) lines::iterator) {
        if (terminateSearch.get()) break;

        if (logLine.line() == null || logLine.line().isBlank()) continue;

        if (handlerList != null && !handlerList.isEmpty()) {
          for (ILogSearchHandler handler : handlerList) {
            handler.handle(logLine);
            boolean found = handler.isFound();
            if (found) {
              // stop scanning further
              terminateSearch.set(true);
              break;
            }
          }
        }
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
