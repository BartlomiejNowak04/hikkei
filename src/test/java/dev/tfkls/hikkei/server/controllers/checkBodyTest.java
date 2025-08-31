package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.checkBody;
import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import io.vertx.core.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CheckBodyTest {

  @Test
  void throwsExceptionWhenBodyIsNull() {
    Map<String, Class<?>> args = new HashMap<>();
    args.put("name", String.class);

    BadDataException ex = assertThrows(BadDataException.class, () -> checkBody(null, args));

    assertEquals("No data provided", ex.getMessage());
  }

  @Test
  void throwsExceptionWhenMissingRequiredParameter() {
    JsonObject body = new JsonObject().put("age", 25);
    Map<String, Class<?>> args = new HashMap<>();
    args.put("name", String.class);

    BadDataException ex = assertThrows(BadDataException.class, () -> checkBody(body, args));

    assertEquals("Missing required parameter: name", ex.getMessage());
  }

  @Test
  void throwsExceptionWhenParameterIsNull() {
    JsonObject body = new JsonObject().put("name", (String) null);
    Map<String, Class<?>> args = new HashMap<>();
    args.put("name", String.class);

    BadDataException ex = assertThrows(BadDataException.class, () -> checkBody(body, args));

    assertEquals("Incorrect parameter: name", ex.getMessage());
  }

  @Test
  void throwsExceptionWhenParameterIsWrongType() {
    JsonObject body = new JsonObject().put("age", "notANumber");
    Map<String, Class<?>> args = new HashMap<>();
    args.put("age", Integer.class);

    BadDataException ex = assertThrows(BadDataException.class, () -> checkBody(body, args));

    assertEquals("Incorrect parameter: age", ex.getMessage());
  }

  @Test
  void passesWhenAllParametersAreCorrect() {
    JsonObject body = new JsonObject().put("name", "Alice").put("age", 30);

    Map<String, Class<?>> args = new HashMap<>();
    args.put("name", String.class);
    args.put("age", Integer.class);

    assertDoesNotThrow(() -> checkBody(body, args));
  }
}
