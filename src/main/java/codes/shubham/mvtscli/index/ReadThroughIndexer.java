package codes.shubham.mvtscli.index;

import java.nio.file.Path;
import java.util.Optional;

public class ReadThroughIndexer {

  private final IndexWriter writer;

  public ReadThroughIndexer(IndexWriter writer) {
    this.writer = writer;
  }

  public void onLine(Path file, String line, long lineNo) {
    extractRequestId(line).ifPresent(reqId -> {
      writer.append(reqId, file, lineNo);
    });
  }

  private Optional<String> extractRequestId(String line) {
    if (line == null || !line.contains("multifleet_planner")) {
      return Optional.empty();
    }

    // remove [ ] if present
    String clean = line.replace("[", "").replace("]", "");

    // keep empty tokens
    String[] parts = clean.split(",", -1);

    for (int i = 0; i < parts.length; i++) {
      if ("multifleet_planner".equals(parts[i].trim())) {
        int reqIndex = i + 4;
        if (reqIndex < parts.length) {
          String reqId = parts[reqIndex].trim();
          if (!reqId.isEmpty()) {
            return Optional.of(reqId);
          }
        }
      }
    }
    return Optional.empty();
  }

}

