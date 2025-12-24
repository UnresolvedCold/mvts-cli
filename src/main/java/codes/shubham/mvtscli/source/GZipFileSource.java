package codes.shubham.mvtscli.source;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class GZipFileSource implements ILogSource {
  private final BufferedReader reader;
  private final String filePath;

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
  public Stream<String> lines() throws IOException {
    return reader.lines();
  }

  @Override
  public void close() throws Exception {
    reader.close();
  }

  @Override
  public String getFilePath() {
    return filePath;
  }
}
