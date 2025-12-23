package codes.shubham.mvtscli.helpers;

import codes.shubham.mvtscli.ApplicationProperties;

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
}
