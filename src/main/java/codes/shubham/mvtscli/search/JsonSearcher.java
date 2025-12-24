package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;

/***
 * Searches message or output JSONs in log lines for a given request ID.
 */
public final class JsonSearcher implements ILogSearcher {
  public void search(ILogSource source, IOSearchMode mode,
                             String requestID, ILogHandler matchHandler,
                     ILogHandler lineHandler) {
    try (source) {
      long[] lineNo = {0};

      source.lines().forEach(line -> {
        lineNo[0]++;
        if (line == null || line.isEmpty()) return;

        if (lineHandler != null)lineHandler.handle(line, lineNo[0]);

        if (!line.contains(requestID)) return;

        int idx = line.indexOf(mode.marker());
        if (idx < 0) return;

        String json = line.substring(idx + mode.marker().length()).trim();
        if (json.isEmpty()) return;

        if (JsonMatcher.matchesRequestId(json, requestID) && matchHandler != null) {
          matchHandler.handle(json, lineNo[0]);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
