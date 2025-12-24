package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.search.ILogHandler;
import codes.shubham.mvtscli.source.ILogSource;
import codes.shubham.mvtscli.source.LogLine;
import codes.shubham.mvtscli.source.PlainFileSource;
import codes.shubham.mvtscli.source.position.Position;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer  {

  private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Indexer.class);

  // requestID -> FilePath -> Set<Position>
  static Map<String, Map<String, IndexPosition>> index = new ConcurrentHashMap<>();

  // FilePath -> ChangeIdentifier
  static Map<String, String> logFileAndChangeIdentifierMap = new ConcurrentHashMap<>();

  // FilePath -> LastIndexedPosition
  static Map<String, Position> logFileAndLastIndexedPositionMap = new ConcurrentHashMap<>();

  public Indexer() {
    logFileAndChangeIdentifierMap.keySet().forEach(this::validate);
  }

  public Map<String, IndexPosition> search(String requestID) {
    return index.getOrDefault(requestID, null);
  }

  public IndexPosition search(String requestID, String filePath) {
    if (!index.containsKey(requestID)) return null;
    return index.get(requestID).getOrDefault(filePath, null);
  }

  public void index(LogLine logline) {
    String requestID = getRequestID(logline.line());

    if (requestID == null || requestID.isBlank()) return;

    boolean isIndexPresent = isIsIndexPresent(logline, requestID);

    if (!isIndexPresent) {
      index.computeIfAbsent(requestID, k -> new ConcurrentHashMap<>());
      index.get(requestID).putIfAbsent(logline.filePath(),
          new IndexPosition(logline.position(), logline.position()));

      IndexPosition old = index.get(requestID).get(logline.filePath());

      logger.trace("indexing requestID: {} in file: {} at position: {}",
          requestID, logline.filePath(), logline.position());

      if (old.start().compare(logline.position()) == 1) {
        logger.trace("Updating start position for requestID: {} in file: {} from {} to {}",
            requestID, logline.filePath(), old.start(), logline.position());
        index.get(requestID).put(logline.filePath(),
            new IndexPosition(logline.position(), old.end()));
      }

      if (old.end().compare(logline.position()) == -1) {
        logger.trace("Updating end position for requestID: {} in file: {} from {} to {}",
            requestID, logline.filePath(), old.end(), logline.position());
        index.get(requestID).put(logline.filePath(),
            new IndexPosition(old.start(), logline.position()));
      }

      logFileAndLastIndexedPositionMap.put(logline.filePath(), logline.position());
    }
  }

  private static boolean isIsIndexPresent(LogLine logline, String requestID) {
    return index.containsKey(requestID)
        && index.get(requestID).containsKey(logline.filePath())
        // Assuming atleast there are 2 log lines for a requestID in a file
        && index.get(requestID).get(logline.filePath()).start() != null
        && index.get(requestID).get(logline.filePath()).end() != null
        && logline.position().compare(index.get(requestID).get(logline.filePath()).end()) <= 0;

  }

  private String getRequestID(String l) {
    final String line = l.substring(0,Math.min(l.length() - 1, 300));
    if (!line.contains("multifleet_planner")) return null;
    return line.split("multifleet_planner")[1].split(",")[3].trim();
  }

  private void validate(String filePath) {
    String changeIdentifier = getChangeIdentifier(filePath);
    String lastChangeIdentifier = logFileAndChangeIdentifierMap.get(filePath);
    Position lastIndexedPosition = logFileAndLastIndexedPositionMap.get(filePath);

    if (lastChangeIdentifier != null && !lastChangeIdentifier.equals(changeIdentifier)) {
      // File has changed since last indexing
      System.out.println("File " + filePath + " has changed since last indexing. Re-indexing from start.");
      logFileAndLastIndexedPositionMap.remove(filePath);
    } else if (lastIndexedPosition != null) {
      System.out.println("File " + filePath + " is up to date. Last indexed position: " + lastIndexedPosition);
    } else {
      System.out.println("File " + filePath + " is being indexed for the first time.");
      logFileAndLastIndexedPositionMap.remove(filePath);
    }

    logFileAndChangeIdentifierMap.put(filePath, changeIdentifier);
  }

  private String getChangeIdentifier(String filePath) {
    // First line of file
    if (filePath.endsWith(".gz")) return "";

    try {
      ILogSource src = new PlainFileSource(Path.of(filePath), 0, 0);
      return src.logLines().findFirst().map(LogLine::line).orElse(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
