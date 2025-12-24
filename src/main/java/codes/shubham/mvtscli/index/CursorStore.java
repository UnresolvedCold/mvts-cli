package codes.shubham.mvtscli.index;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class CursorStore {

  private final Path cursorFile;
  private final ObjectMapper mapper = new ObjectMapper();
  private Map<String, FileCursor> state;

  public CursorStore(Path cursorFile) {
    this.cursorFile = cursorFile;
    this.state = load();
  }

  public synchronized FileCursor get(Path file) {
    return state.get(file.toAbsolutePath().toString());
  }

  public synchronized void update(Path file, long offset) {
    try {
      BasicFileAttributes attr =
          Files.readAttributes(file, BasicFileAttributes.class);

      state.put(
          file.toAbsolutePath().toString(),
          new FileCursor(
              offset,
              attr.size(),
              attr.lastModifiedTime().toMillis()
          )
      );
      persist();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public synchronized boolean isRotated(Path file, FileCursor cursor) {
    try {
      BasicFileAttributes attr =
          Files.readAttributes(file, BasicFileAttributes.class);

      return attr.size() < cursor.size()
          || attr.lastModifiedTime().toMillis() < cursor.lastModified();
    } catch (IOException e) {
      return true;
    }
  }

  private Map<String, FileCursor> load() {
    if (!Files.exists(cursorFile)) {
      return new HashMap<>();
    }
    try {
      return mapper.readValue(
          cursorFile.toFile(),
          new TypeReference<>() {}
      );
    } catch (IOException e) {
      return new HashMap<>();
    }
  }

  private void persist() {
    try {
      mapper.writerWithDefaultPrettyPrinter()
          .writeValue(cursorFile.toFile(), state);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
