package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import io.vertx.core.json.JsonObject;
import java.util.Map;

public class Utils {
  public static int exceptionToCode(Exception e) {
    if (e == null) return 500;
    return switch (e.getClass().getSimpleName()) {
      case "BadDataException", "DecodeException" -> 400;
      case "InternalErrorException" -> 500;
      default -> 500;
    };
  }

  public static void checkBody(JsonObject body, Map<String, Class<?>> arguments)
      throws BadDataException {
    if (body == null) throw new BadDataException("No data provided");

    for (Map.Entry<String, Class<?>> pair : arguments.entrySet()) {
      String argument = pair.getKey();
      Class<?> type = pair.getValue();
      if (!body.containsKey(argument))
        throw new BadDataException("Missing required parameter: " + argument);

      Object arg = body.getValue(argument);
      if (arg == null) throw new BadDataException("Incorrect parameter: " + argument);
      try {
        type.cast(arg);
      } catch (ClassCastException e) {
        throw new BadDataException("Incorrect parameter: " + argument);
      }
    }
  }
}
