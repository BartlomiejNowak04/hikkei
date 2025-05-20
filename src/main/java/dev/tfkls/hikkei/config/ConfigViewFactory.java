package dev.tfkls.hikkei.config;

import java.util.Map;

public class ConfigViewFactory {
  private static final Map<String, String> defaultConfig =
      Map.of(
          "host", "localhost",
          "port", "8080",
          "database", "sqlite3:///hikkei-testing.db");

  public static ConfigView standardConfigView() {
    return (new EnvironmentConfiguration()).merge(defaultConfigView()).freeze();
  }

  public static Configuration defaultConfigView() {
    return new Configuration(defaultConfig);
  }
}
