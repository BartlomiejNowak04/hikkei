package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class FailureHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext ctx) {
    Throwable e = ctx.failure();
    if (!(e instanceof BadDataException) && !(e instanceof InternalErrorException)) {
      ctx.response().setStatusCode(500);
      ctx.json(new JsonObject().put("message", "Internal server error"));
    } else {
      ctx.response().setStatusCode(e instanceof BadDataException ? 400 : 500);
      ctx.json(new JsonObject().put("message", e.getMessage()));
    }
  }
}
