package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;

import java.util.List;

/***
 * Run through logs line by line and handle each line via the provided handler
 ***/
public final class LogRunner {
  public void run(ILogSource source, List<ILogHandler> handlerList) {
    try (source) {
      source
          .logLines()
          .forEach(
              logline -> {
                if (logline.line() == null || logline.line().isBlank()) return;

                if (handlerList != null && !handlerList.isEmpty()) {
                  handlerList.forEach(h->
                    h.handle(logline));
                }
              });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
