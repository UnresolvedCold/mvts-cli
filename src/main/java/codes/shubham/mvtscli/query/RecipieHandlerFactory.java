package codes.shubham.mvtscli.query;

import codes.shubham.mvtscli.query.handler.IQueryHandler;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

public class RecipieHandlerFactory {
  private static volatile RecipieHandlerFactory instance;
  private final Map<String, IQueryHandler> queryHandlerMap = new ConcurrentHashMap<>();

  private RecipieHandlerFactory() {
    loadBuiltIns();
    loadPlugins();
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
    if (!queryHandlerMap.containsKey(recipieName)) {
      throw new IllegalArgumentException(
          "Unknown recipie: " + recipieName);
    }
    return queryHandlerMap.get(recipieName);
  }

  private void loadBuiltIns() {
    ServiceLoader.load(IQueryHandler.class)
        .forEach(this::register);
  }

  private void loadPlugins() {
    PluginLoader.load()
        .forEach(this::register);
  }

  private void register(IQueryHandler handler) {
    String name = handler.name();

    if (queryHandlerMap.containsKey(name)) {
      throw new IllegalStateException(
          "Duplicate query handler: " + name);
    }

    queryHandlerMap.put(name, handler);
  }
}
