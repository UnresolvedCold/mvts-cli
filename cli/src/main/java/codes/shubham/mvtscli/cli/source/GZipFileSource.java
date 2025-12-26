package codes.shubham.mvtscli.cli.source;

import codes.shubham.mvtscli.cli.source.position.LinePosition;

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

  private long startLine = 0;
  private long endLine = -1;

  private boolean finished = false;

  public GZipFileSource(Path path, long startLine, long endLine) throws IOException {
    filePath = path.getFileName().toString();
    this.startLine = Math.max(1, startLine);
    this.endLine = endLine;
    InputStream in = new FileInputStream(path.toFile());
    this.reader = new BufferedReader(
        new InputStreamReader(
            new GZIPInputStream(in),
                StandardCharsets.UTF_8)
        );
  }

  @Override
  public Stream<LogLine> logLines() {

    Iterator<LogLine> it = new Iterator<>() {
      String next;

      @Override
      public boolean hasNext() {
        if (finished) return false;

        try {
          while (true) {
            next = reader.readLine();
            if (next == null) {
              finished = true;
              return false;
            }

            lineNumber++;

            if (lineNumber < startLine) {
              continue;
            }

            if (endLine > 0 && lineNumber > endLine) {
              finished = true;
              return false;
            }

            return true;
          }
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }

      @Override
      public LogLine next() {
        return new LogLine(
            filePath,
            next,
            new LinePosition(lineNumber)
        );
      }
    };

    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(
            it,
            Spliterator.ORDERED | Spliterator.NONNULL
        ),
        false
    );
  }

  @Override
  public void close() throws Exception {
    reader.close();
  }
}