package codes.shubham.mvtscli.source.position;

public record LinePositon(long lineNumber) implements Position {
  @Override
  public int compare(Position other) {
    return 0;
  }
}
