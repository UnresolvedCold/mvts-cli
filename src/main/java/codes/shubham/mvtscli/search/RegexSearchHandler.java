package codes.shubham.mvtscli.search;

public class RegexSearchHandler implements  ILogHandler {

  private final String searchTerm;

  public RegexSearchHandler(String searchTerm) {
    this.searchTerm = searchTerm;
  }

  @Override
  public void handle(String line, long lineNumber, String filePath) {
    if (line == null || line.isEmpty()) return;

    if (line.matches(".*" + searchTerm + ".*")) {
      synchronized (System.out) {
        System.out.println(line);
      }
    }
  }
}
