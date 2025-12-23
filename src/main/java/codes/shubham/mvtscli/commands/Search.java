package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.search.ILogSearcher;
import codes.shubham.mvtscli.search.JsonSearcher;
import codes.shubham.mvtscli.search.RegexSearcher;
import codes.shubham.mvtscli.search.IOSearchMode;
import codes.shubham.mvtscli.source.GZipFileSource;
import codes.shubham.mvtscli.source.ILogSource;
import codes.shubham.mvtscli.source.PlainFileSource;
import org.joda.time.DateTime;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@CommandLine.Command(
    name = "search",
    description = "Search an entity"
)
public class Search implements Runnable {

  ExecutorService pool = Executors.newFixedThreadPool(
      Math.min(1, Runtime.getRuntime().availableProcessors())
  );

  @CommandLine.Parameters(index = "0",
      description = "message|m|output|o|regex|r")
  String type;

  @CommandLine.Parameters(index = "1",
      description = "entity, e.g., request ID or regex")
  String entity;

  @CommandLine.Option(
      names = {"--dates", "-d"},
      description = "Dates to search",
      arity = "0..*"
  )
  List<String> dates;


  @Override
  public void run() {
    IOSearchMode mode = IOSearchMode.getMode(type);
    final ILogSearcher searcher = getSearcher(mode);

    final List<Path> targets = getPaths();

    for (Path file : targets) {
      pool.submit(() -> {
        try {
          ILogSource source =
              file.toString().endsWith(".gz")
                  ? new GZipFileSource(file)
                  : new PlainFileSource(file);

          List<String> res = searcher.search(source, mode, entity);

          synchronized (System.out) {
            res.forEach(System.out::println);
          }

        } catch (Exception e) {
          System.err.println("Failed: " + file + " -> " + e.getMessage());
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

  private ILogSearcher getSearcher(IOSearchMode mode) {
    if (mode == IOSearchMode.REGEX) {
      return new RegexSearcher();
    } else {
      return new JsonSearcher();
    }
  }

  public static void main(String[] args){
    CommandLine commandLine = new CommandLine(new Search());
//    commandLine.execute(new String[]{"message", "rXQOxO1uRLG9tG2VuJMhWw=="});
    commandLine.execute(new String[]{"r", "rXQOxO1uRLG9tG2VuJMhWw==.*before validation"});
//    commandLine = new CommandLine(new Search());
//    commandLine.execute(new String[]{"r", "rXQOxO1uRLG9tG2VuJMhWw==.*before validation","--dates", "2025-12-12"});
//    commandLine = new CommandLine(new Search());
//    commandLine.execute(new String[]{"message", "r","--files", "scheduler.*"});
  }
}
