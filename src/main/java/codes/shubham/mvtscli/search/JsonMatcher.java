package codes.shubham.mvtscli.search;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

public class JsonMatcher {
  private static final JsonFactory FACTORY = new JsonFactory();
  private static final String REQUEST_ID_FIELD = "request_id";

  private JsonMatcher() {}

  public static boolean matchesRequestId(String json, String requestId) {
    if (! json.contains(requestId)) return false;

    try (JsonParser parser = FACTORY.createParser(json)) {

      while (parser.nextToken() != null) {

        if (parser.currentToken() == JsonToken.FIELD_NAME
            && REQUEST_ID_FIELD.equals(parser.getCurrentName())) {

          parser.nextToken(); // move to value

          if (parser.currentToken().isScalarValue()) {
            return requestId.equals(parser.getValueAsString());
          }

          return false; // request_id exists but not scalar
        }
      }

    } catch (IOException e) {
    }
    return false;
  }

}
