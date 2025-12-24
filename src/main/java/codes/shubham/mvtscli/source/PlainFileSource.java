package codes.shubham.mvtscli.source;

import codes.shubham.mvtscli.index.CursorStore;
import codes.shubham.mvtscli.index.FileCursor;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PlainFileSource implements ILogSource {

  private final Path path;
  private final CursorStore cursors;

  public PlainFileSource(Path path, CursorStore cursors) {
    this.path = path;
    this.cursors = cursors;
  }

  @Override
  public Stream<String> lines() {
    try {
      RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");

      FileCursor cursor = cursors.get(path);
      if (cursor != null && !cursors.isRotated(path, cursor)) {
        raf.seek(cursor.offset());
      }

      Iterator<String> iterator = new Iterator<>() {
        String nextLine;

        @Override
        public boolean hasNext() {
          if (nextLine != null) return true;
          try {
            nextLine = raf.readLine();
            return nextLine != null;
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        }

        @Override
        public String next() {
          if (!hasNext()) throw new NoSuchElementException();
          String line = nextLine;
          nextLine = null;
          return line;
        }
      };

      return StreamSupport.stream(
          Spliterators.spliteratorUnknownSize(
              iterator,
              Spliterator.ORDERED | Spliterator.NONNULL
          ),
          false
      ).onClose(() -> {
        try {
          cursors.update(path, raf.getFilePointer());
          raf.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    // no-op (handled by stream close)
  }
}

