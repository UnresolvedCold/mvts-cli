package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.search.ILogSearcher;
import codes.shubham.mvtscli.search.JsonSearcher;
import codes.shubham.mvtscli.search.RegexSearcher;
import codes.shubham.mvtscli.search.IOSearchMode;
import codes.shubham.mvtscli.source.GZipFileSource;
import codes.shubham.mvtscli.source.ILogSource;
import codes.shubham.mvtscli.source.PlainFileSource;
import picocli.CommandLine;

import java.nio.file.Path;
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
      names = {"--files", "-f"},
      description = "Files to search",
      arity = "0..*"
  )
  List<String> files;


  @Override
  public void run() {
    IOSearchMode mode = IOSearchMode.getMode(type);
    final ILogSearcher searcher = getSearcher(mode);

    List<Path> targets;

    if (files == null || files.isEmpty()) {
      targets = FileResolver.resolve("scheduler.log");
    } else if (files.contains("*")) {
      targets = FileResolver.resolve("*");
    } else {
      targets = files.stream()
          .flatMap(f -> {
            try {
              return FileResolver.resolve(f).stream();
            } catch (Exception e) {
              return Stream.empty();
            }
          })
          .toList();
    }

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
//    commandLine.execute(new String[]{"message", "r","--files", "*"});
//    commandLine = new CommandLine(new Search());
//    commandLine.execute(new String[]{"message", "r","--files", "scheduler.*"});
  }
}
