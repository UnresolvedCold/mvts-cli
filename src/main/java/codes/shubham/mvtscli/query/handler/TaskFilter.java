package codes.shubham.mvtscli.query.handler;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.*;

public class TaskFilter extends AbstractFilterHandler {
  protected Map<String, Object> getFilteredOutput  (
      String requestID, String output, String... params) throws Exception {

    String taskKey = params[0];
    Object outputJson = mapper.readValue(output, Object.class);

    DocumentContext pJ = JsonPath.using(conf).parse(outputJson);

    List<?> assignments = pJ.read("$.schedule.assignments[?(@.task_key=='" + taskKey + "')]");
    Map<String, Object> filteredJson = new LinkedHashMap<>();
    filteredJson.put("request_id", requestID);
    filteredJson.put("assignments", assignments);
    return filteredJson;
  }

  protected Map<String, Object> getFilteredMessage(
      String requestID, String message, String... params) throws Exception {
    {
      String taskKey = params[0];
      Object messageJson = mapper.readValue(message, Object.class);

      DocumentContext pJ = JsonPath.using(conf).parse(messageJson);

      List<?> taskData = pJ.read("$.task_list[?(@.task_key=='" + taskKey + "')]");

      Set<String> carriers = new HashSet<>();
      Set<Integer> pps = new HashSet<>();
      Set<String> relayPoints = new HashSet<>();

      for (Object taskObj : taskData) {
        DocumentContext taskDoc = JsonPath.using(conf).parse(taskObj);
        carriers.add(taskDoc.read("@.transport_entity_id"));
        pps.add(taskDoc.read("@.destination_id"));
      }

      List<?> carrierData =
          pJ.read(
              "$.transport_entity_list[?(@.id in " + mapper.writeValueAsString(carriers) + ")]");

      Set<String> carriersAtRelay = new HashSet<>();

      for (Object carrierObj : carrierData) {
        DocumentContext carrierDoc = JsonPath.using(conf).parse(carrierObj);
        String currentLocation = carrierDoc.read("@.current_location");
        if (currentLocation != null) {
          carriersAtRelay.add(carrierDoc.read("@.id"));
        }
      }

      List<?> relayData =
          pJ.read(
              "$.relay_point_list[?(@.reserving_tote_id in " + mapper.writeValueAsString(carriersAtRelay) + ")]");

      Map<String, Object> filteredJson = new LinkedHashMap<>();
      filteredJson.put("request_id", requestID);
      filteredJson.put("task_list", taskData);
      filteredJson.put("transport_entity_list", carrierData);
      filteredJson.put("relay_point_list", relayData);

      return filteredJson;

    }
  }

  @Override
  public String name() {
    return "task";
  }
}
