package codes.shubham.mvtscli.helpers;

import codes.shubham.mvtscli.search.JsonMatcher;
import codes.shubham.mvtscli.search.SearchMode;
import codes.shubham.mvtscli.source.ILogSource;

public final class JsonSearcher {
  public void search(ILogSource source, SearchMode mode, String requestId) throws Exception {

    try (source) {
      source.lines().forEach(line -> {
        int idx = line.indexOf(mode.marker());
        if (idx < 0) return;

        String json = line.substring(idx + mode.marker().length()).trim();
        if (json.isEmpty()) return;

        if (JsonMatcher.matchesRequestId(json, requestId)) {
          System.out.println(json);
        }
      });
    }
  }
}
