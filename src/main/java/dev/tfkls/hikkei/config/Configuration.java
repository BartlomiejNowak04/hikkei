package dev.tfkls.hikkei.config;

import java.util.*;

public class Configuration implements ConfigView {
  Map<String, String> configMapping;

  public Configuration() {
    configMapping = new HashMap<>();
  }
  Configuration(Map<String,String> mapping) {
    configMapping = new HashMap<>(mapping);
  }

  public Configuration add(String key, String value) {
    configMapping.put(key, value);
    return this;
  }

  public Configuration merge(Configuration def) {
    configMapping.putAll(def.configMapping);
    return this;
  }

  public ConfigView freeze() {
    return new Configuration(Map.copyOf(configMapping));
  }

  @Override
  public final String siteHost() {
    return Objects.requireNonNull(configMapping.get("host"));
  }

  @Override
  public final int sitePort() {
    return Integer.parseInt(Objects.requireNonNull(configMapping.get("port")));
  }

  @Override
  public final String databaseUrl() {
    return Objects.requireNonNull(configMapping.get("database"));
  }

  public static final List<String> configKeys = List.of("host", "port", "database");
}
