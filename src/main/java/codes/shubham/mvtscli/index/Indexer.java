package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.search.ILogHandler;
import codes.shubham.mvtscli.source.ILogSource;
import codes.shubham.mvtscli.source.LogLine;
import codes.shubham.mvtscli.source.PlainFileSource;
import codes.shubham.mvtscli.source.position.Position;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Indexer  {

  // requestID -> FilePath -> Set<Position>
  static Map<String, Map<String, Position>> index = new ConcurrentHashMap<>();

  // FilePath -> ChangeIdentifier
  static Map<String, String> logFileAndChangeIdentifierMap = new ConcurrentHashMap<>();

  // FilePath -> LastIndexedPosition
  static Map<String, Position> logFileAndLastIndexedPositionMap = new ConcurrentHashMap<>();

  public Indexer() {
    logFileAndChangeIdentifierMap.keySet().forEach(this::validate);
  }

  public Map<String, Position> search(String requestID) {
    return index.getOrDefault(requestID, null);
  }

  public Position search(String requestID, String filePath) {
    if (!index.containsKey(requestID)) return null;
    return index.get(requestID).getOrDefault(filePath, null);
  }

  public void index(LogLine logline) {
    String requestID = getRequestID(logline.line());

    if (requestID == null || requestID.isBlank()) return;

    boolean isIndexPresent = index.containsKey(requestID)
        && index.get(requestID).containsKey(logline.filePath());

    if (!isIndexPresent) {
      index.computeIfAbsent(requestID, k -> new ConcurrentHashMap<>());
      index.get(requestID).putIfAbsent(logline.filePath(), logline.position());

      Position old = index.get(requestID).get(logline.filePath());

      if (old.compare(logline.position()) == 1) {
        index.get(requestID).put(logline.filePath(), logline.position());
      }

      logFileAndLastIndexedPositionMap.put(logline.filePath(), logline.position());
    }
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
      ILogSource src = new PlainFileSource(Path.of(filePath), 0);
      return src.logLines().findFirst().map(LogLine::line).orElse(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
