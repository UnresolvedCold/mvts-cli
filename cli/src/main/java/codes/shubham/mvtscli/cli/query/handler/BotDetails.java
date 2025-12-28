package codes.shubham.mvtscli.cli.query.handler;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.*;

public class BotDetails extends AbstractFilterHandler {
  protected Map<String, Object> getFilteredOutput  (
      String requestID, String output, String... params) throws Exception {
    Object outputJson = mapper.readValue(output, Object.class);
    DocumentContext pJ = JsonPath.using(conf).parse(outputJson);

    List<Object> filteredAssignments = new ArrayList<>();

    List<?> assignments = pJ.read("$.schedule.assignments[*]");
    for (Object assignmentObj : assignments) {
      DocumentContext assignmentDoc = JsonPath.using(conf).parse(assignmentObj);
      String msuID = assignmentDoc.read("@.transport_entity_id");
      Integer bot = assignmentDoc.read("@.assigned_ranger_id");
      String assignmentType = assignmentDoc.read("@.task_subtype");
      String taskKey = assignmentDoc.read("@.task_key");

      Map<String, Object> assignmentDetail = new LinkedHashMap<>();
      assignmentDetail.put("transport_entity_id", msuID);
      assignmentDetail.put("assigned_ranger_id", bot);
      assignmentDetail.put("task_subtype", assignmentType);
      assignmentDetail.put("task_key", taskKey);

      if (params.length == 1) {
        Integer botID = Integer.parseInt(params[0]);
        if (botID.equals(bot)) {
          filteredAssignments.add(assignmentDetail);
        }
      } else {
        filteredAssignments.add(assignmentDetail);
      }
    }

    return new LinkedHashMap<>() {{;
      put("request_id", requestID);
      put("assignments", filteredAssignments);
    }};
  }

  protected Map<String, Object> getFilteredMessage(
      String requestID, String message, String... params) throws Exception {
    {
      Object messageJson = mapper.readValue(message, Object.class);
      DocumentContext pJ = JsonPath.using(conf).parse(messageJson);

      List<?> bots = null;
      if (params.length == 1) {
        Integer botID = Integer.parseInt(params[0]);
        bots = pJ.read("$.ranger_list[?(@.id=='" + botID + "')]");
      } else {
        bots = pJ.read("$.ranger_list[*]");
      }

      Long startTime = pJ.read("$.start_time");

      List<Object> botDetails = new ArrayList<>();
      for (Object botObj : bots) {
        DocumentContext botDoc = JsonPath.using(conf).parse(botObj);
        Map<String, Object> botDetail = new LinkedHashMap<>();

        Integer botID = botDoc.read("@.id");
        botDetail.put("id", botID);

        List<Object> rangerSchedule = botDoc.read("@.ranger_schedule[*]");
        botDetail.put("current_assignment_count", rangerSchedule.size());

        Long availableAtTime = botDoc.read("@.available_at_time");
        if (availableAtTime!=null) {
          botDetail.put("available_in_ms", (availableAtTime - startTime));
        }

        String status = botDoc.read("@.status");
        botDetail.put("status", status);

        String taskSubtype = botDoc.read("@.task_type");
        if (taskSubtype != null && !taskSubtype.isEmpty()) {
          botDetail.put("current_task_type", taskSubtype);
        }

        String taskKey = botDoc.read("@.task_key");
        if (taskKey != null && !taskKey.isEmpty()) {
          botDetail.put("current_task_key", taskKey);
        }

        Integer aisleId = botDoc.read("@.current_aisle_info.aisle_id");
        if (aisleId != null) {
          botDetail.put("current_aisle_id", aisleId);
        }

        botDetails.add(botDetail);
      }

      Map<String, Object> filteredJson = new LinkedHashMap<>();
      filteredJson.put("request_id", requestID);
      filteredJson.put("bots", botDetails);

      return filteredJson;

    }
  }

  @Override
  public String name() {
    return "bot";
  }
}
