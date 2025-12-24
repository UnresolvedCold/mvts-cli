package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.index.IndexHandler;
import codes.shubham.mvtscli.index.IndexPosition;
import codes.shubham.mvtscli.index.IndexValidateHandler;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.search.*;
import codes.shubham.mvtscli.source.ILogSource;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
    name = "search",
    description = "Search an entity"
)
public class Search implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(Search.class);

  ExecutorService pool = Executors.newFixedThreadPool(
      Math.max(1, Runtime.getRuntime().availableProcessors())
  );

  @CommandLine.Parameters(index = "0",
      description = "message|m|output|o|regex|r")
  String type = "r";

  @CommandLine.Parameters(index = "1",
      description = "request ID")
  String requestID = "";

  @CommandLine.Option(
      names = {"--regex", "-r"},
      description = "regex pattern you want to search",
      arity = "1"
  )
  String regexPattern = "";

  @CommandLine.Option(
      names = {"--dates", "-d"},
      description = "dates to search",
      arity = "0..*"
  )
  List<String> dates;

  static boolean s = false;

  @Override
  public void run() {
    final List<Path> targets = getPaths();

    Indexer indexer = new Indexer();

    logger.debug("Searching files: {}", targets);

    for (Path file : targets) {
      pool.submit(() -> {
        long offset1 = 0;
        long offset2 = -1;
        boolean searchingIndexed = false;

        IndexPosition position = indexer.search(requestID, file.getFileName().toString());

        if (position != null) {
          logger.debug("Found index position for {} in file {}: {} - {}",
              requestID, file, position.start(), position.end());
          offset1 = position.start().offset();
          offset2 = position.end().offset();
          searchingIndexed = true;
        }

        try {
          ILogSource source = FileResolver.getSource(file, offset1, offset2);

          LogRunner runner = new LogRunner();
          List<ILogHandler> handlers = new ArrayList<>();

          IOSearchMode mode = IOSearchMode.getMode(type);

          if (mode == IOSearchMode.MESSAGE || mode == IOSearchMode.OUTPUT) {
            logger.trace("Searching for input/output, requestID: {}", requestID);
            handlers.add(new MessageSearchHandler(requestID, mode.marker()));
          } else if (mode == IOSearchMode.REGEX) {
            logger.trace("Searching for regex pattern, requestID: {}, regex: {}", requestID, regexPattern);
            handlers.add(new RegexSearchHandler(requestID, regexPattern));
          } else {
            throw new IllegalArgumentException("Unsupported search type: " + type);
          }

          runner.run(source, handlers);
        } catch (Exception e) {
          logger.error("Failed: " + file + " -> " + e.getMessage());
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

  private List<Path> getPaths() {
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

  public static void main(String[] args){

    {
      CommandLine commandLine = new CommandLine(new Search());
      long start = System.currentTimeMillis();
      commandLine.execute(new String[] {"r", "a_cold"});
      long end = System.currentTimeMillis();
      System.out.println("Time taken: " + (end - start) + " ms");
    }

    {
      CommandLine commandLine = new CommandLine(new Search());
      long start = System.currentTimeMillis();
      commandLine.execute(new String[] {"o", "b_cold"});
      long end = System.currentTimeMillis();
      System.out.println("Time taken: " + (end - start) + " ms");
    }

    {
      CommandLine commandLine = new CommandLine(new Search());
      long start = System.currentTimeMillis();
      commandLine.execute(new String[] {"o", "c_cold"});
      long end = System.currentTimeMillis();
      System.out.println("Time taken: " + (end - start) + " ms");
    }

    {
      CommandLine commandLine = new CommandLine(new Search());
      long start = System.currentTimeMillis();
      commandLine.execute(new String[] {"m", "d_cold"});
      long end = System.currentTimeMillis();
      System.out.println("Time taken: " + (end - start) + " ms");
    }
  }
}
