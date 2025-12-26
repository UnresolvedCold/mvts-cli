package codes.shubham.mvtscli.cli.index;

import codes.shubham.mvtscli.cli.ApplicationProperties;
import codes.shubham.mvtscli.cli.helpers.FileResolver;
import codes.shubham.mvtscli.cli.source.LogLine;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;

public class Indexer  {

  private IndexData indexData;
  private IndexData tempIndexData;
  private boolean isIndexUpdated = false;
  private boolean freshIndex = false;

  private final Path INDEX_FILE;

  private final static Logger logger = org.slf4j.LoggerFactory.getLogger(Indexer.class);

  public Indexer(String indexFileName, boolean freshIndex) {
    INDEX_FILE = Path.of(ApplicationProperties.USER_HOME_DIR.getValue(),
        ApplicationProperties.MVTS_HOME_DIR.getValue(),
        indexFileName);
    this.freshIndex = freshIndex;
    init();
  }

  public Indexer(String indexFileName) {
    this(indexFileName, false);
  }

  public Map<String, Map<String, IndexPosition>> getAllIndexData() {
    return indexData.getAll();
  }

  public IndexPosition search(String requestID, String filePath) {
    return indexData.getIndexPositon(requestID, filePath);
  }

  public void index(LogLine logline) {
    IndexData indexData = tempIndexData;

    String requestID = FileResolver.getRequestIDFromLogLine(logline.line());

    if (requestID == null || requestID.isBlank()) return;

    boolean isIndexPresent = isIndexPresent(logline, requestID);

    if (!isIndexPresent) {
      if (indexData.getIndexPositon(requestID, logline.filePath()) == null) {
        indexData.setIndexPosition(
            requestID,
            logline.filePath(),
            new IndexPosition(logline.position(), logline.position()));
      }

      IndexPosition old = indexData.getIndexPositon(requestID, logline.filePath());

      if (old.start().compare(logline.position()) == 1) {
        indexData.setIndexPosition(requestID, logline.filePath(),
            new IndexPosition(logline.position(), old.end()));
      }

      if (old.end().compare(logline.position()) == -1) {
        indexData.setIndexPosition(requestID, logline.filePath(),
            new IndexPosition(old.start(), logline.position()));
      }

      isIndexUpdated = true;
    }
  }

  private boolean isIndexPresent(LogLine logline, String requestID) {
    if (freshIndex) return false;
    return indexData.getIndexPositon(requestID, logline.filePath()) != null
        && indexData.getIndexPositon(requestID, logline.filePath()).start() != null
        && indexData.getIndexPositon(requestID, logline.filePath()).end() != null
        && logline.position().compare(indexData.getIndexPositon(requestID, logline.filePath()).end()) <= 0;

  }


  public void validate(LogLine logline) {
    throw new RuntimeException("not implemented yet");
  }

  private void commitToFile() {
    try{
      Files.createDirectories(INDEX_FILE.getParent());

      Path tmp = INDEX_FILE.resolveSibling(INDEX_FILE.getFileName() + ".tmp");

      Files.writeString(
          tmp,
          indexData.toString(),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING
      );

      Files.move(
          tmp,
          INDEX_FILE,
          java.nio.file.StandardCopyOption.REPLACE_EXISTING,
          java.nio.file.StandardCopyOption.ATOMIC_MOVE
      );

      logger.debug("Index committed to {}", INDEX_FILE);

    } catch (Exception e) {
      logger.error("Failed to commit index", e);
    }
  }

  private IndexData loadFromFile() {
    if (!Files.exists(INDEX_FILE)) {
      logger.debug("No index file found at {}", INDEX_FILE);
      return new IndexData();
    }

    try {
      String content = Files.readString(INDEX_FILE, StandardCharsets.UTF_8);
      logger.debug("Index loaded from {}", INDEX_FILE);
      return IndexData.fromString(content);

    } catch (IOException e) {
      logger.error("Failed to load index", e);
      return new IndexData();
    }
  }

  private void commitIndex() {
    if (!isIndexUpdated) return;
    indexData = tempIndexData;
  }

  public void commit() {
    commitIndex();
    commitToFile();
    tempIndexData = indexData;
  }

  public void init() {
    indexData = loadFromFile();
    if (freshIndex) tempIndexData = new IndexData();
    else tempIndexData = indexData;
  }
}
