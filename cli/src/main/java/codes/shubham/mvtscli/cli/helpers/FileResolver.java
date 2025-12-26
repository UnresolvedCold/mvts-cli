package codes.shubham.mvtscli.cli.helpers;

import codes.shubham.mvtscli.cli.ApplicationProperties;
import codes.shubham.mvtscli.cli.source.GZipFileSource;
import codes.shubham.mvtscli.cli.source.ILogSource;
import codes.shubham.mvtscli.cli.source.PlainFileSource;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class FileResolver {

  public static List<Path> resolve(String pattern) {
    Path dir = Paths.get(ApplicationProperties.LOG_DIR.getValue());
    PathMatcher matcher =
        FileSystems.getDefault().getPathMatcher("glob:" + pattern);

    // Search latest logs first
    try (Stream<Path> s = Files.list(dir)) {
      return s
          .filter(p -> matcher.matches(p.getFileName()))
          .sorted((a, b) -> {
            try {
              long a1 = Files.getLastModifiedTime(a).toMillis();
              long b1 = Files.getLastModifiedTime(b).toMillis();
              return Long.compare(b1, a1);
            } catch (IOException e) {}

            return 0;
          })
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static ILogSource getSource(Path file, long offset1, long offset2) throws IOException {
    String name = file.getFileName().toString();

    if (name.endsWith(".gz")) {
      return new GZipFileSource(file, offset1, offset2);
    }

    return new PlainFileSource(file, offset1, offset2);
  }

  public static String getRequestIDFromLogLine(String l) {
    final String line = l.substring(0,Math.min(l.length() - 1, 300));
    if (!line.contains("multifleet_planner")) return null;
    return line.split("multifleet_planner")[1].split(",")[3].trim();
  }

}
