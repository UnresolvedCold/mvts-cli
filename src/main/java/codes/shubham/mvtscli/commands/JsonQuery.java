package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.ApplicationProperties;
import codes.shubham.mvtscli.handlers.ILogHandler;
import codes.shubham.mvtscli.handlers.MessageSearchHandler;
import codes.shubham.mvtscli.handlers.RegexSearchHandler;
import codes.shubham.mvtscli.handlers.searchresult.ISearchedResultHandler;
import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.helpers.Tuple2;
import codes.shubham.mvtscli.index.IndexPosition;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.query.JsonData;
import codes.shubham.mvtscli.query.handler.IQueryHandler;
import codes.shubham.mvtscli.query.handler.JsonPathQueryHandler;
import codes.shubham.mvtscli.query.handler.TaskFilter;
import codes.shubham.mvtscli.search.IOSearchMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@CommandLine.Command(
    name = "json",
    description = "Query a searched json entity"
)
public class JsonQuery extends AbstractLogRunnerCommand implements Runnable {

  private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JsonQuery.class);

  // query string
  // if starting with r (for recipie) then it's a recipie query
  // else it is a jmespath query
  @CommandLine.Parameters(index = "0",
      description = "query")
  String query;

  @Override
  public void run() {
    Indexer indexer = new Indexer(ApplicationProperties.RECENT_SEARCH_INDEX_FILE.getValue());

    Set<String> reqIds = indexer.getAllIndexData().keySet();

    logger.trace("Recently searched request IDs: {}", reqIds);


    Map<String, JsonData> requestIDToJsonData = new ConcurrentHashMap<>();

    final List<Indexer> indexers = getIndexers();


    final List<Tuple2<IOSearchMode, ISearchedResultHandler>> modes
        = getModes(requestIDToJsonData);

    handleAndCollectJsondata(reqIds, modes, indexer, indexers);

    logger.trace("Collected JSON data for request IDs: {}", requestIDToJsonData.keySet());

    List<Object> results = new ArrayList<>();

    requestIDToJsonData.forEach(
        (k, v) -> {
          IQueryHandler handler = new JsonPathQueryHandler();
          var res = handler.handle(k, v.message(), v.output(), query);
          results.add(res);
        });

    try {
      ObjectMapper mapper = new ObjectMapper();
      String finalOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);
      System.out.println(finalOutput);
    } catch (Exception ignored){}
  }

  private void handleAndCollectJsondata(Set<String> reqIds, List<Tuple2<IOSearchMode, ISearchedResultHandler>> modes, Indexer indexer, List<Indexer> indexers) {
    for (String requestID: reqIds) {
      List<ILogHandler> handlers = new ArrayList<>();

      for (Tuple2<IOSearchMode, ISearchedResultHandler> mode : modes) {
        handlers.add(
            new MessageSearchHandler(
                requestID, mode.getFirst().marker(), l -> mode.getSecond().handle(l)));
        }

        Set<String> files = indexer.getAllIndexData().get(requestID).keySet();
        List<Path> targets = new ArrayList<>();
        for (String file: files) {
          targets.add(Path.of(ApplicationProperties.LOG_DIR.getValue(), file));
        }

        handle(requestID, targets, indexers, handlers);
    }
  }

  private List<Tuple2<IOSearchMode, ISearchedResultHandler>> getModes(Map<String, JsonData> requestIDToJsonData) {
    List<Tuple2<IOSearchMode, ISearchedResultHandler>> modes =
        List.of(
            new Tuple2<>(
                IOSearchMode.MESSAGE,
                l -> {
                  String json = l.split(IOSearchMode.MESSAGE.marker())[1].trim();
                  synchronized (requestIDToJsonData) {
                    JsonData existing = requestIDToJsonData.getOrDefault(
                        FileResolver.getRequestIDFromLogLine(l),
                        new JsonData(null, null)
                    );
                    requestIDToJsonData.put(
                        FileResolver.getRequestIDFromLogLine(l),
                        new JsonData(json, existing.output())
                    );
                  }
                }),
            new Tuple2<>(
                IOSearchMode.OUTPUT,
                l -> {
                  String json = l.split(IOSearchMode.OUTPUT.marker())[1].trim();
                  synchronized (requestIDToJsonData) {
                    JsonData existing = requestIDToJsonData.getOrDefault(
                        FileResolver.getRequestIDFromLogLine(l),
                        new JsonData(null, null)
                    );
                    requestIDToJsonData.put(
                        FileResolver.getRequestIDFromLogLine(l),
                        new JsonData(existing.message(), json)
                    );
                  }
                }));
    return modes;
  }

  private List<Indexer> getIndexers() {
    List<Indexer> indexers = new ArrayList<>(List.of(
        new Indexer(ApplicationProperties.RECENT_SEARCH_INDEX_FILE.getValue()),
        new Indexer(ApplicationProperties.MAIN_INDEX_FILE.getValue())
    ));
    return indexers;
  }

  public static void main(String[] args){
    //    CommandLine.run(new JsonQuery(), "1b987314-259d-4cb8-aebf-501fc15970fa");
    CommandLine.run(new JsonQuery(),
        "$.schedule.assignments[?(@.task_key=='1b987314-259d-4cb8-aebf-501fc15970fa')]");
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
