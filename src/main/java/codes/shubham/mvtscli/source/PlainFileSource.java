package codes.shubham.mvtscli.source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class PlainFileSource implements ILogSource {
  private final Path path;

  public PlainFileSource(Path path) {
    this.path = path;
  }

  @Override
  public Stream<String> lines() throws IOException {
    return Files.lines(path);
  }

  @Override
  public void close() throws Exception {

  }
}
