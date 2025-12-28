package codes.shubham.mvtscli.cli.query.handler;

import codes.shubham.mvtscli.cli.helpers.Helper;
import codes.shubham.mvtscli.plugin.query.handler.IQueryHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PPSDetails implements IQueryHandler {
  ObjectMapper mapper = Helper.getObjectMapper();

  @Override
  public Map<String, Object> handle(String requestID, String message, String output, String... params) {
    String ppsID = params[0];
    Object jsonObj = null;
    try {
      jsonObj = mapper.readValue(message, Object.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    DocumentContext pJ = JsonPath.parse(jsonObj);

    List<?> ppsList = pJ.read("$.pps_list[?(@.id=='" + ppsID + "')]");
    List<?> taskList = pJ.read("$.task_list[?(@.destination_id=='" + ppsID + "')]");

    Map<String, Object> res = new LinkedHashMap<>();
    res.put("tasks", taskList);
    res.put("task_count", taskList.size());
    res.put("request_id", requestID);
    res.put("pps", ppsList);
    return res;
  }

  @Override
  public String name() {
    return "pps";
  }

  @Override
  public String description() {
    return "PPS Details";
  }
}
