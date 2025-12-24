package codes.shubham.mvtscli.source;

import codes.shubham.mvtscli.source.position.BytePosition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PlainFileSource implements ILogSource {
  private final Path path;
  private FileChannel channel;
  private BufferedReader reader;
  private String filePath;

  public PlainFileSource(Path path, int byteOffset) {
    this.path = path;
    this.filePath = path.getFileName().toString();
    try {
    openAt(byteOffset);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void openAt(long offset) throws IOException {
    this.channel = FileChannel.open(path, StandardOpenOption.READ);
    this.channel.position(offset);
    this.reader = new BufferedReader(
        new InputStreamReader(Channels.newInputStream(channel), StandardCharsets.UTF_8)
    );

    // resync if offset is not zero
    if (offset > 0) {
      reader.readLine(); // discard partial line
    }
  }

  @Override
  public Stream<LogLine> logLines() {
    Iterator<LogLine> it =
        new Iterator<>() {
          String next;
          long offset;

          @Override
          public boolean hasNext() {
            try {
              offset = channel.position();
              next = reader.readLine();
              return next != null;
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }

          @Override
          public LogLine next() {
            return new LogLine(filePath, next, new BytePosition(offset));
          }
        };

        return StreamSupport.stream(
            Spliterators.spliteratorUnknownSize(
                it,
                Spliterator.ORDERED | Spliterator.NONNULL
            ),
            false
        )
        .onClose(() -> {
          try {
            close();
          } catch (Exception ignored) {}
        });
  }

  @Override
  public void close() throws Exception {
    if (reader != null) reader.close();
    if (channel != null) channel.close();
  }
}
