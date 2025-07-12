package dev.tfkls.hikkei.server.routing.handler;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public class ArgumentCheckHandler implements Handler<RoutingContext> {
  private final List<String> arguments;

  public ArgumentCheckHandler(List<String> arguments) {
    this.arguments = arguments;
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();
    for (String argument : arguments) {
      if (!body.containsKey(argument)) {
        response.setStatusCode(400);
        response.end(
            new JsonObject().put("message", "Missing required parameter: " + argument).encode());
        return;
      }

      Object arg = body.getValue(argument);
      if (arg == null) {
        response.setStatusCode(400);
        response.end(new JsonObject().put("message", "Incorrect parameter: " + argument).encode());
        return;
      }
    }

    ctx.next();
  }
}
