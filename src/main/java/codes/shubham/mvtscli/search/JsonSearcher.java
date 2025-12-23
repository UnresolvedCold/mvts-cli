package codes.shubham.mvtscli.search;

import codes.shubham.mvtscli.source.ILogSource;

import java.util.ArrayList;
import java.util.List;

/***
 * Searches message or output JSONs in log lines for a given request ID.
 */
public final class JsonSearcher implements ILogSearcher {
  public List<String> search(ILogSource source, IOSearchMode mode, String requestID){
    List<String> results = new ArrayList<>();
    try (source) {
      source.lines().forEach(line -> {
        if (line == null || line.isEmpty() || !line.contains(requestID)) return;
        int idx = line.indexOf(mode.marker());
        if (idx < 0) return;

        String json = line.substring(idx + mode.marker().length()).trim();
        if (json.isEmpty()) return;

        if (JsonMatcher.matchesRequestId(json, requestID)) {
          results.add(json);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return results;
  }
}
