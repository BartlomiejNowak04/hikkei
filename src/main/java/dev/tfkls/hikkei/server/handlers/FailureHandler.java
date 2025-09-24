package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

public class FailureHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext ctx) {
    Throwable e = ctx.failure();
    if (ctx.response().ended()) {
      return;
    }
    if (!(e instanceof BadDataException)
        && !(e instanceof InternalErrorException)
        && !(e instanceof AuthenticationException)
        && !(e instanceof HttpException)) {
      ctx.response().setStatusCode(500);
      ctx.json(new JsonObject().put("message", "Internal server error"));
      // ctx.json(new JsonObject().put("message", e.toString()));
    } else if (e instanceof HttpException) {
      ctx.response().setStatusCode(((HttpException) e).getStatusCode());
      ctx.json(
          new JsonObject()
              .put(
                  "message",
                  e.getCause() != null
                      ? e.getCause().getMessage()
                      : ((HttpException) e).getPayload() != null
                          ? ((HttpException) e).getPayload()
                          : e.getMessage()));
    } else {
      ctx.response().setStatusCode(e instanceof InternalErrorException ? 500 : 400);
      ctx.json(new JsonObject().put("message", e.getMessage()));
    }
  }
}
