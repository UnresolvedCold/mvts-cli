package codes.shubham.mvtscli.source.position;

public sealed interface Position permits LinePositon, BytePosition {
  public int compare(Position other);
}

