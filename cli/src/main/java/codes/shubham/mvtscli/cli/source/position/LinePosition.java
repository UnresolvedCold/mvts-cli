package codes.shubham.mvtscli.cli.source.position;

public record LinePosition(long lineNumber) implements Position {
  @Override
  public int compare(Position other) {
    if (other instanceof LinePosition(long ln)) {
      return Long.compare(this.lineNumber, ln);
    }
    throw  new RuntimeException();
  }

  @Override
  public long offset() {
    return lineNumber;
  }
}
