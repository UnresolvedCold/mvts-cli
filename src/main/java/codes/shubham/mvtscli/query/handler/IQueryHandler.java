package codes.shubham.mvtscli.query.handler;

import java.util.Map;

public interface IQueryHandler {
  public Map<String, Object> handle(
      String requestID,
      String message,
      String output,
      String ...params);
}
