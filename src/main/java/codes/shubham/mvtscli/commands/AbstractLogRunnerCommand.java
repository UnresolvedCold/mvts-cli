package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.handlers.ILogHandler;
import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.index.IndexPosition;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.search.LogRunner;
import codes.shubham.mvtscli.source.ILogSource;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractLogRunnerCommand {
  private ExecutorService pool = Executors.newFixedThreadPool(
      Math.max(1, Runtime.getRuntime().availableProcessors())
  );

  protected abstract Logger getLogger();

  protected void handle(String requestID, List<Path> targets, List<Indexer> indexers, List<ILogHandler> handlers) {
    for (Path file : targets) {
      pool.submit(
          () -> {
            long offset1 = 0;
            long offset2 = -1;

            for (Indexer indexer : indexers) {
              IndexPosition position = indexer.search(requestID, file.getFileName().toString());

              if (position != null) {
                getLogger().debug(
                    "Found index position for {} in file {}: {} - {}",
                    requestID,
                    file,
                    position.start(),
                    position.end());

                offset1 = position.start().offset();
                offset2 = position.end().offset();
              }
            }

            try {
              ILogSource source = FileResolver.getSource(file, offset1, offset2);

              LogRunner runner = new LogRunner();

              runner.run(source, handlers);
            } catch (Exception e) {
              getLogger().error("Failed: " + file + " -> " + e.getMessage());
            }
          });
    }

    pool.shutdown();
    try {
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  protected List<Path> getPaths(List<String> dates) {
    List<Path> targets = new ArrayList<>();

    if (dates == null || dates.isEmpty()) {
      return FileResolver.resolve("scheduler.log");
    }

    List<DateTime> dateTimes = dates.stream()
        .map(DateTime::parse)
        .toList();

    DateTime today = DateTime.now().withTimeAtStartOfDay();

    boolean containsToday = dateTimes.stream()
        .anyMatch(dt -> dt.withTimeAtStartOfDay().isEqual(today));

    if (containsToday) {
      targets.addAll(FileResolver.resolve("scheduler.log"));
    }

    for (DateTime dt : dateTimes) {
      if (dt.withTimeAtStartOfDay().isEqual(today)) {
        continue;
      }

      String date = dt.toString("yyyy-MM-dd");
      String pattern = "scheduler." + date + ".*.log.gz";

      try {
        targets.addAll(FileResolver.resolve(pattern));
      } catch (Exception ignored) {
        // intentionally ignore missing logs
      }
    }

    return targets;
  }

}
