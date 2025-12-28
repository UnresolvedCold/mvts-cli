package codes.shubham.mvtscli.cli.query.handler;

import codes.shubham.mvtscli.plugin.query.handler.IQueryHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Option;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractFilterHandler implements IQueryHandler {

  protected static final ObjectMapper mapper = new ObjectMapper();
  protected final Configuration conf =
      Configuration.builder()
          .options(Option.SUPPRESS_EXCEPTIONS)
          .build();

  public Map<String, Object> handle(String requestID, String message, String output, String... params) {
    Map<String, Object> filteredMessage = new HashMap<>();
    try {
      filteredMessage = getFilteredMessage(requestID, message, params);
    } catch (Exception ignored) {
      System.out.println();
    }
    Map<String, Object> filteredOutput = new HashMap<>();
    try {
      filteredOutput = getFilteredOutput(requestID, output, params);
    } catch (Exception ignored) {
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("request_id", requestID);
    result.put("message", filteredMessage);
    result.put("output", filteredOutput);
    return result;
  }

  protected abstract Map<String, Object> getFilteredMessage(
      String requestID, String messageJson, String ... params) throws Exception;
  protected abstract Map<String, Object> getFilteredOutput(
      String requestID, String outputJson, String ... params) throws Exception;

  @Override
  public String description() {
    return this.getClass().getSimpleName();
  }

  @Override
  public String name() {
    return this.getClass().getSimpleName();
  }
}
