package codes.shubham.mvtscli.index;

import codes.shubham.mvtscli.source.position.Position;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IndexData {
  // requestID -> FilePath -> Set<Position>
  private final Map<String, Map<String, IndexPosition>> index = new ConcurrentHashMap<>();
  private final Map<String, Position> fileStatus = new ConcurrentHashMap<>();

  public IndexPosition getIndexPositon(String requestId, String filePath) {
    if (!index.containsKey(requestId)) return null;
    return index.get(requestId).getOrDefault(filePath, null);
  }

  public void setIndexPosition(String requestId, String filePath, IndexPosition indexPosition) {
    index.computeIfAbsent(requestId, k -> new ConcurrentHashMap<>());
    index.get(requestId).put(filePath, indexPosition);

    fileStatus.putIfAbsent(filePath, indexPosition.end());
    if (fileStatus.get(filePath).compare(indexPosition.end()) == 1) {
      fileStatus.put(filePath, indexPosition.end());
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    // index
    index.forEach((requestId, fileMap) -> {
      fileMap.forEach((filePath, indexPosition) -> {
        if (indexPosition == null ||
            indexPosition.start() == null ||
            indexPosition.end() == null) {
          return;
        }

        sb.append("IDX|")
            .append(requestId).append('|')
            .append(filePath).append('|')
            .append(indexPosition.start()).append('|')
            .append(indexPosition.end())
            .append('\n');
      });
    });

    fileStatus.forEach((filePath, position) -> {
      sb.append("FST|")
          .append(filePath).append('|')
          .append(position)
          .append('\n');
    });

    return sb.toString();
  }

  public static IndexData fromString(String string) {
    IndexData data = new IndexData();

    if (string == null || string.isBlank()) {
      return data;
    }

    String[] lines = string.split("\n");

    for (String line : lines) {
      try {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 2) continue;

        switch (parts[0]) {
          case "IDX" -> {
            if (parts.length != 5) continue;

            String requestId = parts[1];
            String filePath = parts[2];
            Position start = Position.fromString(parts[3]);
            Position end = Position.fromString(parts[4]);

            data.index
                .computeIfAbsent(requestId, k -> new ConcurrentHashMap<>())
                .put(filePath, new IndexPosition(start, end));
          }

          case "FST" -> {
            if (parts.length != 3) continue;

            String filePath = parts[1];
            Position position = Position.fromString(parts[2]);

            data.fileStatus.put(filePath, position);
          }
        }
      } catch (Exception ignore) {
        // skip malformed line
      }
    }

    return data;
  }

  public Map<String, Map<String, IndexPosition>> getAll() {
    return index;
  }
}
