package codes.shubham.mvtscli.search;

public enum SearchMode {
  MESSAGE("message", " Message: "),
  OUTPUT("output", " Output: ");

  String key;
  String marker;

  SearchMode(String key, String marker) {
    this.key = key;
    this.marker = marker;
  }

  public String marker() {
    return marker;
  }
}
