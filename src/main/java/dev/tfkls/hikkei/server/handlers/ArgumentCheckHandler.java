package dev.tfkls.hikkei.server.handlers;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

public class ArgumentCheckHandler implements Handler<RoutingContext> {
  private final Map<String, Class<?>> arguments;

  public ArgumentCheckHandler(Map<String, Class<?>> arguments) {
    this.arguments = arguments;
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();
    for (Map.Entry<String, Class<?>> pair : arguments.entrySet()) {
      String argument = pair.getKey();
      Class<?> type = pair.getValue();

      if (!body.containsKey(argument)) {
        throwError(400, "Missing required parameter: " + argument, response);
        return;
      }

      boolean type_check = true;
      Object arg = body.getValue(argument);
      try {
        type.cast(arg);
      } catch (ClassCastException ex) {
        type_check = false;
      }

      if (arg == null || !type_check) {
        throwError(400, "Incorrect parameter: " + argument, response);
        return;
      }
    }

    ctx.next();
  }
}
