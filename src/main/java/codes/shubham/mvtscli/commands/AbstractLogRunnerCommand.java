package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.handlers.ILogHandler;
import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.index.IndexPosition;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.search.LogRunner;
import codes.shubham.mvtscli.source.ILogSource;
import org.slf4j.Logger;

import java.nio.file.Path;
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


}
