package codes.shubham.mvtscli.query.handler;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.Map;

public class JsonPathQueryHandler extends AbstractFilterHandler{

  @Override
  protected Map<String, Object> getFilteredMessage(String requestID, String messageJson, String... params) throws Exception {
    String query = params[0];
    Object messageObj = mapper.readValue(messageJson, Object.class);
    DocumentContext pJ = JsonPath.using(conf).parse(messageObj);

    return Map.of("result", pJ.read(query));
  }

  @Override
  protected Map<String, Object> getFilteredOutput(String requestID, String outputJson, String... params) throws Exception {
    String query = params[0];
    Object outputObj = mapper.readValue(outputJson, Object.class);
    DocumentContext pJ = JsonPath.using(conf).parse(outputObj);

    return Map.of("result", pJ.read(query));
  }

  @Override
  public String name() {
    return "jmespath";
  }
}
