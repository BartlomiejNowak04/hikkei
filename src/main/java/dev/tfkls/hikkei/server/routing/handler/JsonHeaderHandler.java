package dev.tfkls.hikkei.server.routing.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class JsonHeaderHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext ctx) {
    ctx.response().putHeader("Content-Type", "application/json");
    ctx.next();
  }
}
