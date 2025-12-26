package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.ApplicationProperties;
import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.index.IndexPosition;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.search.*;
import codes.shubham.mvtscli.handlers.ILogHandler;
import codes.shubham.mvtscli.handlers.MessageSearchHandler;
import codes.shubham.mvtscli.handlers.RegexSearchHandler;
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
public class Search extends AbstractLogRunnerCommand implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(Search.class);

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
    final List<Path> targets = getPaths(dates);


    List<Indexer> indexers = new ArrayList<>(List.of(
        new Indexer(ApplicationProperties.RECENT_SEARCH_INDEX_FILE.getValue()),
        new Indexer(ApplicationProperties.MAIN_INDEX_FILE.getValue())
    ));

    final List<ILogHandler> handlers = getLogHandlers();

    logger.debug("Searching files: {}", targets);

    handle(requestID, targets, indexers, handlers);
  }


  private List<ILogHandler> getLogHandlers() {
    List<ILogHandler> handlers = new ArrayList<>();

    IOSearchMode mode = IOSearchMode.getMode(type);

    if (mode == IOSearchMode.MESSAGE || mode == IOSearchMode.OUTPUT) {
      logger.trace("Searching for input/output, requestID: {}", requestID);
      handlers.add(new MessageSearchHandler(requestID, mode.marker(), l ->{
        synchronized (System.out) {
          System.out.println(l.split(mode.marker())[1].trim());
        }
      }));
    } else if (mode == IOSearchMode.REGEX) {
      logger.trace("Searching for regex pattern, requestID: {}, regex: {}", requestID, regexPattern);
      handlers.add(new RegexSearchHandler(requestID, regexPattern, l->{
        synchronized (System.out) {
          System.out.println(l);
        }
      }));
    } else {
      throw new IllegalArgumentException("Unsupported search type: " + type);
    }
    return handlers;
  }

  public static void main(String[] args){

    {
      CommandLine commandLine = new CommandLine(new Search());
      long start = System.currentTimeMillis();
      commandLine.execute(new String[] {"o", "apbxfVhCQJKQws63f4TUng==", "-d", "2025-12-04", "-d", "2025-12-25"});
      long end = System.currentTimeMillis();
      System.out.println("Time taken: " + (end - start) + " ms");
    }

//    {
//      CommandLine commandLine = new CommandLine(new Search());
//      long start = System.currentTimeMillis();
//      commandLine.execute(new String[] {"o", "b_cold"});
//      long end = System.currentTimeMillis();
//      System.out.println("Time taken: " + (end - start) + " ms");
//    }
//
//    {
//      CommandLine commandLine = new CommandLine(new Search());
//      long start = System.currentTimeMillis();
//      commandLine.execute(new String[] {"o", "c_cold"});
//      long end = System.currentTimeMillis();
//      System.out.println("Time taken: " + (end - start) + " ms");
//    }
//
//    {
//      CommandLine commandLine = new CommandLine(new Search());
//      long start = System.currentTimeMillis();
//      commandLine.execute(new String[] {"m", "d_cold"});
//      long end = System.currentTimeMillis();
//      System.out.println("Time taken: " + (end - start) + " ms");
//    }
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
