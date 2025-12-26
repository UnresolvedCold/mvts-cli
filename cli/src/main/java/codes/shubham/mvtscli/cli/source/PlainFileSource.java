package codes.shubham.mvtscli.cli.source;

import codes.shubham.mvtscli.cli.source.position.BytePosition;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
public class PlainFileSource implements ILogSource, AutoCloseable {

  private final Path path;
  private final String filePath;
  private final long endOffset;

  private FileChannel channel;
  private MappedByteBuffer buffer;
  private long absoluteStart;
  private long absoluteEnd;

  public PlainFileSource(Path path, long startOffset, long endOffset) throws IOException {
    this.path = path;
    this.filePath = path.getFileName().toString();
    this.endOffset = endOffset;
    openAt(startOffset);
  }

  private void openAt(long startOffset) throws IOException {
    this.channel = FileChannel.open(path, StandardOpenOption.READ);
    long size = channel.size();

    this.absoluteStart = startOffset;
    this.buffer = channel.map(
        FileChannel.MapMode.READ_ONLY,
        startOffset,
        size - startOffset
    );

    this.absoluteEnd = findEndOfLine(endOffset, size);
  }

  private long findEndOfLine(long lineStart, long fileSize) {
    if (lineStart >= fileSize || lineStart < 0) {
      return fileSize;
    }

    int relativePos = (int) (lineStart - absoluteStart);
    int limit = buffer.limit();

    for (int i = relativePos; i < limit; i++) {
      if (buffer.get(i) == '\n') {
        return absoluteStart + i + 1;
      }
    }

    return fileSize;
  }

  @Override
  public Stream<LogLine> logLines() {
    Iterator<LogLine> it = new Iterator<>() {
      long currentOffset = absoluteStart;

      @Override
      public boolean hasNext() {
        return buffer.hasRemaining() && currentOffset < absoluteEnd;
      }

      @Override
      public LogLine next() {
        long lineStart = currentOffset;
        String line = readLine();
        currentOffset = absoluteStart + buffer.position();
        return new LogLine(filePath, line, new BytePosition(lineStart));
      }
    };

    return StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(it, Spliterator.ORDERED | Spliterator.NONNULL),
        false
    ).onClose(() -> {
      try {
        close();
      } catch (Exception ignored) {}
    });
  }

  private String readLine() {
    StringBuilder sb = new StringBuilder();

    while (buffer.hasRemaining() && (absoluteStart + buffer.position()) < absoluteEnd) {
      byte b = buffer.get();
      if (b == '\n') break;
      sb.append((char) b);
    }

    return sb.toString();
  }

  @Override
  public void close() throws Exception {
    if (channel != null) channel.close();
  }
}

