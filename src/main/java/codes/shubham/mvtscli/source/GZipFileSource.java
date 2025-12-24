package codes.shubham.mvtscli.source;

import codes.shubham.mvtscli.source.position.LinePositon;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.GZIPInputStream;

public class GZipFileSource implements ILogSource {
  private final BufferedReader reader;
  private final String filePath;
  private long lineNumber = 0;

  public GZipFileSource(Path path) throws IOException {
    filePath = path.getFileName().toString();
    InputStream in = new FileInputStream(path.toFile());
    this.reader = new BufferedReader(
        new InputStreamReader(
            new GZIPInputStream(in),
                StandardCharsets.UTF_8)
        );
  }

  @Override
  public Stream<LogLine> logLines() throws IOException {
    Iterator<LogLine> it =
        new Iterator<>() {
          String next;

          @Override
          public boolean hasNext() {
            try {
              next = reader.readLine();
              return next != null;
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public LogLine next() {
            return new LogLine(filePath, next, new LinePositon(++lineNumber));
          }
        };

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED | Spliterator.NONNULL),
        false
    );
  }

  @Override
  public void close() throws Exception {
    reader.close();
  }

}
