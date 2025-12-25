package codes.shubham.mvtscli.query;

import codes.shubham.mvtscli.query.handler.IQueryHandler;

import java.util.Map;

public class QueryHandlerFactory {
  private static QueryHandlerFactory instance;
  private Map<String, IQueryHandler> queryHandlerMap;

  private QueryHandlerFactory() {
  }

  public static QueryHandlerFactory getInstance() {
    if (instance == null) {
      synchronized (QueryHandlerFactory.class) {
        if (instance == null) {
          instance = new QueryHandlerFactory();
        }
      }
    }
    return instance;
  }
}
