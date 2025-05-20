package dev.tfkls.hikkei.config;

class EnvironmentConfiguration extends Configuration {
  EnvironmentConfiguration() {
    super();

    for (var key : Configuration.configKeys) {
      configMapping.put(key, System.getenv("HIKKEI_" + key.toUpperCase()));
    }
  }
}
