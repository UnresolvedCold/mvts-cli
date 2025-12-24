package codes.shubham.mvtscli.index;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class IndexWriter {

  private final Path indexFile;

  public IndexWriter(Path indexFile) {
    this.indexFile = indexFile;
  }

  public synchronized void append(String requestId, Path file, long lineNo) {
    String entry = String.format(
        "{\"requestId\":\"%s\",\"file\":\"%s\",\"line\":%d}%n",
        escape(requestId),
        escape(file.toString()),
        lineNo
    );

    try (BufferedWriter writer = Files.newBufferedWriter(
        indexFile,
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE,
        StandardOpenOption.APPEND
    )) {
      writer.write(entry);
    } catch (IOException e) {
      throw new RuntimeException("Failed to write index", e);
    }
  }

  private String escape(String s) {
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
