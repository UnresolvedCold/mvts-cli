package codes.shubham.mvtscli.source.position;

public sealed interface Position permits LinePositon, BytePosition {
  public int compare(Position other);
  public long offset();

  public static Position fromString(String s) {
    // example: "BytePosition[offset=12345]"
    long offset = Long.parseLong(s.replaceAll("\\D+", ""));
    return new BytePosition(offset);
  }
}

