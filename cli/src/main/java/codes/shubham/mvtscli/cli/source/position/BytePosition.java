package codes.shubham.mvtscli.cli.source.position;

public record BytePosition(long byteOffset) implements Position {
  @Override
  public int compare(Position other) {
    if (other instanceof BytePosition(long offset)) {
      return Long.compare(this.byteOffset, offset);
    }
    throw  new RuntimeException();
  }

  @Override
  public long offset() {
    return byteOffset;
  }
}
