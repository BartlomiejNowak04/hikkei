package dev.tfkls.hikkei.server.util;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.util.ThrowingConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;

public class ParamFlow {
  public static JsonObject checkBody(RoutingContext ctx, Map<String, Type> arguments)
      throws BadDataException {
    JsonObject body;
    try {
      body = ctx.body().asJsonObject();
    } catch (Exception e) {
      throw new BadDataException("Invalid body provided");
    }
    if (body == null) throw new BadDataException("No data provided");

    JsonObject res = new JsonObject();
    for (Map.Entry<String, Type> pair : arguments.entrySet()) {
      String argument = pair.getKey();
      Type type = pair.getValue();
      if (!body.containsKey(argument))
        throw new BadDataException("Missing required parameter: " + argument);

      try {
        switch (type) {
          case Int:
            res.put(argument, body.getInteger(argument));
            break;
          case Str:
            res.put(argument, body.getString(argument));
            break;
          case Bool:
            res.put(argument, body.getBoolean(argument));
            break;
          case Binary:
            res.put(argument, body.getBinary(argument));
            break;
        }
      } catch (ClassCastException | IllegalArgumentException e) {
        throw new BadDataException("Incorrect parameter: " + argument);
      }

      if (res.getValue(argument) == null)
        throw new BadDataException("Incorrect parameter: " + argument);
    }

    return res;
  }

  public static JsonObject checkPath(RoutingContext ctx, Map<String, Type> arguments)
      throws BadDataException {
    JsonObject res = new JsonObject();
    for (Map.Entry<String, Type> pair : arguments.entrySet()) {
      String argument = pair.getKey();
      Type type = pair.getValue();

      String arg = ctx.pathParam(argument);
      if (arg == null) throw new BadDataException("Missing required parameter: " + argument);
      try {
        switch (type) {
          case Int:
            res.put(argument, Integer.valueOf(arg));
            break;
          case Str:
            res.put(argument, arg);
            break;
          case Binary:
            res.put(argument, new JsonObject().put("tmp", arg).getBinary("tmp"));
            break;
        }
      } catch (ClassCastException | IllegalArgumentException e) {
        throw new BadDataException("Incorrect parameter: " + argument);
      }
    }

    return res;
  }

  public static void handle(
      RoutingContext ctx,
      Map<String, Type> params,
      Map<String, Type> pathParams,
      ThrowingConsumer<JsonObject> handler) {
    try {
      JsonObject body = new JsonObject();
      if (params != null) body.mergeIn(checkBody(ctx, params));
      if (pathParams != null) body.mergeIn(checkPath(ctx, pathParams));
      handler.accept(body);
    } catch (Exception e) {
      ctx.fail(e);
    }
  }
}
