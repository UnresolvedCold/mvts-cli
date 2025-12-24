package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.index.IndexHandler;
import codes.shubham.mvtscli.index.IndexPosition;
import codes.shubham.mvtscli.index.IndexValidateHandler;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.search.*;
import codes.shubham.mvtscli.source.ILogSource;
import codes.shubham.mvtscli.source.position.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@CommandLine.Command(
    name = "index",
    description = "Will start indexing the logs for a faster search"
)
public class Index implements Runnable {

  private static Logger logger = LoggerFactory.getLogger(Index.class);

  @CommandLine.Option(
      names = {"--interval", "-i"},
      description = "poll interval in seconds to reindex logs",
      arity = "1"
  )
  int intervalInSeconds = 5;

  @Override
  public void run() {
    lockFile();

    List<Path> targets = getPaths();
    Indexer indexer = new Indexer();

    logger.info("Indexing started for {}", targets);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Shutting down indexer...");
      indexer.commit();
    }));

    while (true) {
      try {
        for (Path file : targets) {
          indexFile(file, indexer);
        }

        indexer.commit();
        Thread.sleep(intervalInSeconds * 1000L);

      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      } catch (Exception e) {
        logger.error("Indexing error", e);
      }
    }

    indexer.commit();
  }

  private void lockFile() {
    Path lockFile = Path.of(System.getProperty("user.home"), ".mvts", ".index.lock");

    try (FileChannel lockChannel =
        FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

      FileLock lock = lockChannel.tryLock();

      if (lock == null) {
        System.err.println("Indexer already running");
        System.exit(1);
      }

      Files.writeString(
          lockFile,
          "pid=" + ProcessHandle.current().pid() +
              "\nstarted=" + Instant.now(),
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING
      );

      Runtime.getRuntime()
          .addShutdownHook(
              new Thread(
                  () -> {
                    try {
                      lock.release();
                      lockChannel.close();
                    } catch (Exception ignored) {
                    }
                  }));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void indexFile(Path file, Indexer indexer) throws Exception {
    ILogSource source = FileResolver.getSource(
        file,0, -1
    );

    LogRunner runner = new LogRunner();
    runner.run(source, List.of(new IndexHandler(indexer)));
  }


  private List<Path> getPaths() {
    return FileResolver.resolve("*");
  }

  public static void main(String[] args) throws Exception {
    Thread t1 = new Thread(new Runnable() {
      @Override
      public void run() {
        CommandLine commandLine = new CommandLine(new Index());
        commandLine.execute(new String[] {});
      }
    });

    t1.start();
    t1.join();
  }
}
