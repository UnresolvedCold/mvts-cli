package codes.shubham.mvtscli.search;

public interface ILogHandler {
  public void handle(String line, long lineNumber, String filePath);
}
