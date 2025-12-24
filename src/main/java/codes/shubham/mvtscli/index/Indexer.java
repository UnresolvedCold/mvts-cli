package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.source.LogLine;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer  {

  private IndexData indexData;
  private IndexData tempIndexData;
  private boolean isIndexUpdated = false;

  private static final Path INDEX_FILE =
      Path.of(System.getProperty("user.home"), ".mvts", "index.db");

  private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Indexer.class);

  public Indexer() {
    indexData = loadFromFile();
    tempIndexData = new IndexData();
  }

  public IndexPosition search(String requestID, String filePath) {
    if (!indexData.getIndex().containsKey(requestID)) return null;
    return indexData.getIndex().get(requestID).getOrDefault(filePath, null);
  }

  public void index(LogLine logline) {
    IndexData indexData = tempIndexData;

    String requestID = getRequestID(logline.line());

    if (requestID == null || requestID.isBlank()) return;

    boolean isIndexPresent = isIndexPresent(logline, requestID);

    if (!isIndexPresent) {
      indexData.getIndex().computeIfAbsent(requestID, k -> new ConcurrentHashMap<>());
      indexData.getIndex().get(requestID).putIfAbsent(logline.filePath(),
          new IndexPosition(logline.position(), logline.position()));

      IndexPosition old = indexData.getIndex().get(requestID).get(logline.filePath());

      if (old.start().compare(logline.position()) == 1) {
        indexData.getIndex().get(requestID).put(logline.filePath(),
            new IndexPosition(logline.position(), old.end()));
      }

      if (old.end().compare(logline.position()) == -1) {
        indexData.getIndex().get(requestID).put(logline.filePath(),
            new IndexPosition(old.start(), logline.position()));
      }

      isIndexUpdated = true;
    }
  }

  private boolean isIndexPresent(LogLine logline, String requestID) {
    return indexData.getIndex().containsKey(requestID)
        && indexData.getIndex().get(requestID).containsKey(logline.filePath())
        // Assuming atleast there are 2 log lines for a requestID in a file
        && indexData.getIndex().get(requestID).get(logline.filePath()).start() != null
        && indexData.getIndex().get(requestID).get(logline.filePath()).end() != null
        && logline.position().compare(indexData.getIndex().get(requestID).get(logline.filePath()).end()) <= 0;

  }

  private String getRequestID(String l) {
    final String line = l.substring(0,Math.min(l.length() - 1, 300));
    if (!line.contains("multifleet_planner")) return null;
    return line.split("multifleet_planner")[1].split(",")[3].trim();
  }

  public void validate(LogLine logline) {
    String requestID = getRequestID(logline.line());
    if (requestID == null || requestID.isBlank()) return;

    Map<String, IndexPosition> p1 = indexData.getIndex().get(requestID);

    if (p1 == null) {
      return;
    }

    IndexPosition pos = p1.get(logline.filePath());

    if (pos == null) {
      logger.trace("Invalidating index for requestID {}", requestID);
      indexData.getIndex().remove(requestID);
      return;
    }

    if (logline.position().offset() < pos.start().offset()
        || logline.position().offset() > pos.end().offset()) {
      logger.trace("Invalidating index for requestID {}", requestID);
      indexData.getIndex().remove(requestID);
      return;
    }
  }

  private void commitToFile() {
    try{
      Files.createDirectories(INDEX_FILE.getParent());

      Path tmp = INDEX_FILE.resolveSibling(INDEX_FILE.getFileName() + ".tmp");

      Files.writeString(
          tmp,
          indexData.toString(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING
      );

      Files.move(
          tmp,
          INDEX_FILE,
          java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE
      );

      logger.debug("Index committed to {}", INDEX_FILE);

    } catch (Exception e) {
      logger.error("Failed to commit index", e);
    }
  }

  private IndexData loadFromFile() {
    if (!Files.exists(INDEX_FILE)) {
      logger.debug("No index file found at {}", INDEX_FILE);
      return new IndexData();
    }

    try {
      String content = Files.readString(INDEX_FILE, StandardCharsets.UTF_8);
      logger.debug("Index loaded from {}", INDEX_FILE);
      return IndexData.fromString(content);

    } catch (IOException e) {
      logger.error("Failed to load index", e);
      return new IndexData();
    }
  }

  private void commitIndex() {
    if (!isIndexUpdated) return;
    indexData = tempIndexData;
  }

  public void commit() {
    commitIndex();
    commitToFile();
  }
}
