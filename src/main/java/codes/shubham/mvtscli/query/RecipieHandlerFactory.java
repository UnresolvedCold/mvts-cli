package codes.shubham.mvtscli.query;

import codes.shubham.mvtscli.query.handler.IQueryHandler;
import codes.shubham.mvtscli.query.handler.JsonPathQueryHandler;
import codes.shubham.mvtscli.query.handler.TaskFilter;

import java.util.Map;

public class RecipieHandlerFactory {
  private static RecipieHandlerFactory instance;
  private Map<String, IQueryHandler> queryHandlerMap;

  private RecipieHandlerFactory() {
    this.queryHandlerMap = Map.of(
        "task", new TaskFilter(),
        "jmespath", new JsonPathQueryHandler()
    );
  }

  public static RecipieHandlerFactory getInstance() {
    if (instance == null) {
      synchronized (RecipieHandlerFactory.class) {
        if (instance == null) {
          instance = new RecipieHandlerFactory();
        }
      }
    }
    return instance;
  }

  public IQueryHandler getRecipie(String recipieName) {
    return queryHandlerMap.get(recipieName);
  }
}
