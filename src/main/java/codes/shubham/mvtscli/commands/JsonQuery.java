package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.ApplicationProperties;
import codes.shubham.mvtscli.index.Indexer;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.util.Set;

@CommandLine.Command(
    name = "json",
    description = "Query a searched json entity"
)
public class JsonQuery implements Runnable {

  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JsonQuery.class);

  @CommandLine.Parameters(index = "0",
      description = "query")
  String query;

  @Override
  public void run() {
    Indexer indexer = new Indexer(ApplicationProperties.RECENT_SEARCH_INDEX_FILE.getValue());
    Set<String> reqIds = indexer.getAllIndexData().keySet();
    logger.trace("Recently searched request IDs: {}", reqIds);
  }

  public static void main(String[] args){
    CommandLine.run(new JsonQuery(), "fr");
  }
}
