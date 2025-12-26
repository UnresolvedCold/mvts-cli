package codes.shubham.mvtscli.cli.query;

import codes.shubham.mvtscli.cli.ApplicationProperties;
import codes.shubham.mvtscli.plugin.query.handler.IQueryHandler;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;

public final class PluginLoader {

  private static final Path PLUGIN_DIR =
      Paths.get(
          ApplicationProperties.USER_HOME_DIR.getValue(),
          ApplicationProperties.MVTS_HOME_DIR.getValue(),
          "plugins");

  public static Collection<IQueryHandler> load() {
    if (!Files.isDirectory(PLUGIN_DIR)) {
      return List.of();
    }

    try {
      List<URL> jars = Files.list(PLUGIN_DIR)
          .filter(p -> p.toString().endsWith(".jar"))
          .map(PluginLoader::toURL)
          .toList();

      if (jars.isEmpty()) {
        return List.of();
      }

      ClassLoader pluginClassLoader =
          new URLClassLoader(jars.toArray(URL[]::new),
              IQueryHandler.class.getClassLoader());

      ServiceLoader<IQueryHandler> loader =
          ServiceLoader.load(IQueryHandler.class, pluginClassLoader);

      List<IQueryHandler> handlers = new ArrayList<>();
      for (IQueryHandler handler : loader) {
        handlers.add(handler);
      }
      return handlers;

    } catch (IOException e) {
      throw new RuntimeException("Failed to load plugins", e);
    }
  }

  private static URL toURL(Path p) {
    try {
      return p.toUri().toURL();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

