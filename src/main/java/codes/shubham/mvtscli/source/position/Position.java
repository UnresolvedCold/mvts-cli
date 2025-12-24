package codes.shubham.mvtscli.source.position;

public sealed interface Position permits LinePosition, BytePosition {
  public int compare(Position other);
  public long offset();

  public static Position fromString(String s) {
    // example: "BytePosition[offset=12345]"
    String g = s;
    long offset = Long.parseLong(s.replaceAll("\\D+", ""));
    if (g.contains("LinePosition")) {
      return new LinePosition(offset);
    } else {
      return new BytePosition(offset);
    }
  }
}

