package dev.tfkls.hikkei.test;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.github.cdimascio.dotenv.DotenvBuilder;
import io.github.cdimascio.dotenv.DotenvEntry;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class IntegrationTest {
  private final Map<String, String> environment;

  public IntegrationTest() {
    var dotenv = new DotenvBuilder().filename(".test-env").ignoreIfMissing().load();

    environment =
        Map.copyOf(
            dotenv.entries().stream()
                .collect(Collectors.toMap(DotenvEntry::getKey, DotenvEntry::getValue)));
  }

  public String require(String name) {
    assumeTrue(environment.containsKey(name));
    return environment.get(name);
  }
}
