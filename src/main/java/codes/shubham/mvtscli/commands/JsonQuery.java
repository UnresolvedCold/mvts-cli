package codes.shubham.mvtscli.commands;

import codes.shubham.mvtscli.ApplicationProperties;
import codes.shubham.mvtscli.handlers.ILogHandler;
import codes.shubham.mvtscli.handlers.MessageSearchHandler;
import codes.shubham.mvtscli.handlers.searchresult.ISearchedResultHandler;
import codes.shubham.mvtscli.helpers.FileResolver;
import codes.shubham.mvtscli.helpers.Tuple2;
import codes.shubham.mvtscli.index.Indexer;
import codes.shubham.mvtscli.query.JsonData;
import codes.shubham.mvtscli.query.QueryType;
import codes.shubham.mvtscli.query.RecipieHandlerFactory;
import codes.shubham.mvtscli.query.handler.IQueryHandler;
import codes.shubham.mvtscli.query.handler.JsonPathQueryHandler;
import codes.shubham.mvtscli.search.IOSearchMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
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

  @CommandLine.Parameters(index = "0",
      description = "jmespath|j|recipie|r")
  String queryType;

  // query string
  // if starting with r (for recipie) then it's a recipie query
  // else it is a jmespath query
  @CommandLine.Parameters(index = "1",
      description = "query or recipie name")
  String query;

  @CommandLine.Option(
      names = {"--params", "-p"},
      description = "dates to search",
      arity = "0..*"
  )
  List<String> params;

  @CommandLine.Option(
      names = {"--output", "-o"},
      description = "dates to search",
      arity = "1"
  )
  String outputQuery;

  @CommandLine.Option(
      names = {"-em", "--exclude-message"},
      description = "will only output modified message json",
      defaultValue = "false"
  )
  boolean excludeMessage;

  @CommandLine.Option(
      names = {"-eo", "--exclude-output"},
      description = "will only output modified output json",
      defaultValue = "false"
  )
  boolean excludeOutput;

  @CommandLine.Option(
      names = {"--request-ids", "-ids"},
      description = "if provided, will only process these request IDs",
      arity = "0..*"
  )
  List<String> requestIDs;

  @CommandLine.Option(
      names = {"--dates", "-d"},
      description = "dates to search",
      arity = "0..*"
  )
  List<String> dates;

  @Override
  public void run() {
    Indexer indexer = new Indexer(ApplicationProperties.RECENT_SEARCH_INDEX_FILE.getValue());
    Map<String, JsonData> requestIDToJsonData = new ConcurrentHashMap<>();

    QueryType qt = QueryType.fromString(queryType);
    IQueryHandler queryHandler = getQueryHandler(qt);

    Set<String> reqIds = null;

    if (requestIDs != null && !requestIDs.isEmpty()) {
      reqIds = new HashSet<>(requestIDs);
    } else {
      reqIds = indexer.getAllIndexData().keySet();
    }

    logger.trace("Recently searched request IDs: {}", reqIds);

    final List<Indexer> indexers = getIndexers();
    final List<Tuple2<IOSearchMode, ISearchedResultHandler>> modes
        = getModes(requestIDToJsonData);

    handleAndCollectJsondata(reqIds, modes, indexer, indexers);

    logger.trace("Collected JSON data for request IDs: {}", requestIDToJsonData.keySet());

    List<Object> results = new ArrayList<>();

    requestIDToJsonData.forEach(
        (k, v) -> {
          List<String> finalParams = new ArrayList<>();

          if (qt == QueryType.JMESPATH) {
            finalParams.add(query);
          }

          if (qt == QueryType.RECIPIE) {
            if (params != null) {
              finalParams.addAll(params);
            }
          }

          var res = queryHandler.handle(k, v.message(), v.output(), finalParams.toArray(String[]::new));
          results.add(res);
        });

    try {
      ObjectMapper mapper = new ObjectMapper();
      String finalOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);

      if (outputQuery != null && !outputQuery.isEmpty()) {
        Object outputObj = mapper.readValue(finalOutput, Object.class);
        Configuration conf =
            Configuration.builder().options(Option.SUPPRESS_EXCEPTIONS).build();
        DocumentContext pJ = JsonPath.using(conf).parse(outputObj);
        Object queriedOutput = pJ.read(outputQuery);
        finalOutput = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(queriedOutput);
      }

      System.out.println(finalOutput);
    } catch (Exception ignored) {
    }
  }

  private IQueryHandler getQueryHandler(QueryType qt) {
    if (qt == QueryType.JMESPATH) {
      return new JsonPathQueryHandler();
    } else if (qt == QueryType.RECIPIE) {
      return RecipieHandlerFactory.getInstance().getRecipie(query);
    } else {
      throw new IllegalArgumentException("Unsupported QueryType: " + qt);
    }
  }

  private void handleAndCollectJsondata(Set<String> reqIds, List<Tuple2<IOSearchMode, ISearchedResultHandler>> modes, Indexer indexer, List<Indexer> indexers) {
    for (String requestID : reqIds) {
      List<ILogHandler> handlers = new ArrayList<>();

      for (Tuple2<IOSearchMode, ISearchedResultHandler> mode : modes) {
        handlers.add(
            new MessageSearchHandler(
                requestID, mode.getFirst().marker(), l -> mode.getSecond().handle(l)));
      }

      Set<String> files = indexer.getAllIndexData()
          .getOrDefault(requestID, new HashMap<>()).keySet();

      List<Path> targets = new ArrayList<>();

      for (String file : files) {
        targets.add(Path.of(ApplicationProperties.LOG_DIR.getValue(), file));
      }

      if (dates != null && !dates.isEmpty()) {
        targets = getPaths(dates);
      }

      handle(requestID, targets, indexers, handlers);
    }
  }

  private List<Tuple2<IOSearchMode, ISearchedResultHandler>> getModes(Map<String, JsonData> requestIDToJsonData) {
    List<Tuple2<IOSearchMode, ISearchedResultHandler>> modes = new ArrayList<>();
    if (!excludeMessage) {
      modes.add(new Tuple2<>(
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
          }));
    }

    if (!excludeOutput) {
      modes.add(new Tuple2<>(
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
    }
    return modes;
  }

  private List<Indexer> getIndexers() {
    List<Indexer> indexers = new ArrayList<>(List.of(
        new Indexer(ApplicationProperties.RECENT_SEARCH_INDEX_FILE.getValue()),
        new Indexer(ApplicationProperties.MAIN_INDEX_FILE.getValue())
    ));
    return indexers;
  }

  public static void main(String[] args) {
        CommandLine.run(new JsonQuery(), "r","task","-p", "1b987314-259d-4cb8-aebf-501fc15970fa",
            "-o", "$[0].message", "-ids", "apbxfVhCQJKQws63f4TUng==", "--dates", "2025-12-04");
//    CommandLine.run(new JsonQuery(),"j",
//        "$.schedule.assignments[?(@.task_key=='1b987314-259d-4cb8-aebf-501fc15970fa')]");
  }

  @Override
  protected Logger getLogger() {
    return logger;
  }
}
