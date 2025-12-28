package codes.shubham.mvtscli.cli.query.handler;

import codes.shubham.mvtscli.plugin.query.handler.IQueryHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.*;

public class AisleDetails extends AbstractFilterHandler {

  Map<String, Object> msuDetailsCache = new HashMap<>();
  Map<String, Object> relayDetailsCache = new HashMap<>();

  @Override
  protected Map<String, Object> getFilteredMessage(String requestID, String messageJson, String... params) throws Exception {
    int aisleID = Integer.parseInt(params[0]);

    Object jsonObj = mapper.readValue(messageJson, Object.class);
    DocumentContext pJ = JsonPath.using(conf).parse(jsonObj);

    List<?> taskList = pJ.read("$.task_list[?(@.aisle_info.aisle_id=='" + aisleID + "')]");
    List<?> msuList = pJ.read("$.transport_entity_list[?(@.aisle_info.aisle_id=='" + aisleID + "')]");
    List<?> relayList = pJ.read("$.relay_point_list[?(@.aisle_info.aisle_id=='" + aisleID + "')]");
    List<?> rangerList = pJ.read("$.ranger_list[?(@.current_aisle_info.aisle_id=='" + aisleID + "')]");

    msuList.forEach(m -> {
      try {
        DocumentContext msuDoc = JsonPath.using(conf).parse(m);
        String msuID = msuDoc.read("@.id");
        if (!msuDetailsCache.containsKey(msuID)) {
          msuDetailsCache.put(msuID, m);
        }
      } catch (Exception e) {
        // Ignore
      }
    });

    relayList.forEach(r -> {
      try {
        DocumentContext relayDoc = JsonPath.using(conf).parse(r);
        String relayID = relayDoc.read("@.id");
        if (!relayDetailsCache.containsKey(relayID)) {
          relayDetailsCache.put(relayID, r);
        }
      } catch (Exception e) {
        // Ignore
      }
    });

    int totalAvailableRelays = 0;

    if (relayList != null) {
      for (Object relayObj : relayList) {
        DocumentContext relayDoc = JsonPath.using(conf).parse(relayObj);
        Boolean isAvailable = relayDoc.read("@.status").equals("available");
        if (isAvailable) {
          totalAvailableRelays += 1;
        }
      }
    }

    Map<String, List<Object>> botVersionMap = new HashMap<>();
    for (Object rangerObj : rangerList) {
      DocumentContext rangerDoc = JsonPath.using(conf).parse(rangerObj);
      String botVersion = rangerDoc.read("@.version");
      botVersionMap.putIfAbsent(botVersion, new ArrayList<>());
      botVersionMap.get(botVersion).add(rangerObj);
    }

    Map<String, Integer> botVersions = new HashMap<>();
    for (Map.Entry<String, List<Object>> entry : botVersionMap.entrySet()) {
      botVersions.put(entry.getKey(), entry.getValue().size());
    }

    Map<String, Object> res = new LinkedHashMap<>();
    res.put("task_list", taskList);
    res.put("request_id", requestID);
    res.put("transport_entity_list", msuList);
    res.put("relay_point_list", relayList);
    res.put("ranger_list", rangerList);
    res.put("summary", Map.of(
        "total_relays", relayList == null ? 0 : relayList.size(),
        "total_available_relays", totalAvailableRelays,
        "bot_versions", botVersions
    ));
    return res;
  }

  @Override
  protected Map<String, Object> getFilteredOutput(String requestID, String outputJson, String... params) throws Exception {
    int aisleID = Integer.parseInt(params[0]);

    Object jsonObj = mapper.readValue(outputJson, Object.class);
    DocumentContext pJ = JsonPath.using(conf).parse(jsonObj);


    Set<Integer> bots = new HashSet<>();

    List<?> assignments = pJ.read("$.schedule.assignments[*]");
    Map<String, List<Object>> filteredAssignments = new HashMap<>();
    for (Object assignmentObj : assignments) {
      DocumentContext assignmentDoc = JsonPath.using(conf).parse(assignmentObj);
      String msuID = assignmentDoc.read("@.transport_entity_id");
      String relayID = assignmentDoc.read("@.destination_id");
      Integer bot = assignmentDoc.read("@.assigned_ranger_id");
      String assignmentType = assignmentDoc.read("@.task_subtype");
      if (msuDetailsCache.containsKey(msuID) || relayDetailsCache.containsKey(relayID)) {
        filteredAssignments.putIfAbsent(assignmentType, new ArrayList<>());
        filteredAssignments.get(assignmentType).add(assignmentObj);
        bots.add(bot);
      }
    }

    Map<String, Object> summary = new HashMap<>();
    summary.put("total_bots_assigned", bots.size());

    filteredAssignments.forEach((k, v)->{
      summary.put(k, v.size());
    });

    return Map.of(
        "request_id", requestID,
        "assignments", filteredAssignments.values(),
        "summary", summary
    );
  }

  @Override
  public String name() {
    return "aisle";
  }

  @Override
  public String description() {
    return "Takes aisle ID as parameter and returns details related to that aisle from message and output.";
  }
}
