package codes.shubham.mvtscli.plugin.query.handler;

import java.util.Map;

public interface IQueryHandler {
  public Map<String, Object> handle(
      String requestID,
      String message,
      String output,
      String ...params);

  public String name();
  public String description();
}
