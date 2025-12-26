package codes.shubham.mvtscli.cli.query;

import codes.shubham.mvtscli.plugin.query.handler.IQueryHandler;

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
    queryHandlerMap.putIfAbsent(handler.name(), handler);
  }
}
