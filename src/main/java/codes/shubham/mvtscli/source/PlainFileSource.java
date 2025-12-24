package codes.shubham.mvtscli.source;

import codes.shubham.mvtscli.source.position.BytePosition;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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
  private FileChannel channel;
  private MappedByteBuffer buffer;

  public PlainFileSource(Path path) throws IOException {
    this(path, 0L);
  }

  public PlainFileSource(Path path, long startOffset) throws IOException {
    this.path = path;
    this.filePath = path.getFileName().toString();
    openAt(startOffset);
  }

  private void openAt(long offset) throws IOException {
    this.channel = FileChannel.open(path, StandardOpenOption.READ);
    long size = channel.size();
    this.buffer = channel.map(FileChannel.MapMode.READ_ONLY, offset, size - offset);
  }

  @Override
  public Stream<LogLine> logLines() throws IOException {
    Iterator<LogLine> it = new Iterator<>() {
      private long currentOffset = channel.position();
      private String nextLine;

      @Override
      public boolean hasNext() {
        nextLine = readLine();
        return nextLine != null;
      }

      @Override
      public LogLine next() {
        LogLine line = new LogLine(filePath, nextLine, new BytePosition(currentOffset));
        currentOffset += nextLine.getBytes(StandardCharsets.UTF_8).length + 1; // +1 for '\n'
        return line;
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
    if (!buffer.hasRemaining()) return null;

    StringBuilder sb = new StringBuilder();
    while (buffer.hasRemaining()) {
      byte b = buffer.get();
      if (b == '\n') break;
      sb.append((char) b);
    }

    return sb.length() == 0 && !buffer.hasRemaining() ? null : sb.toString();
  }

  @Override
  public void close() throws Exception {
    if (channel != null) channel.close();
  }

}
