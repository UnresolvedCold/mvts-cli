package codes.shubham.mvtscli.source;

import codes.shubham.mvtscli.source.position.Position;

import java.util.Objects;

public record LogLine(
    String filePath,
    String line,
    Position position
) {

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof LogLine logLine)) return false;

    return Objects.equals(filePath, logLine.filePath) && Objects.equals(position, logLine.position);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(filePath);
    result = 31 * result + Objects.hashCode(position);
    return result;
  }
}
