package dev.tfkls.hikkei.server.routes;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public abstract class RouteHandler {
  protected final Router router;

  public RouteHandler(Vertx vertx) {
    this.router = Router.router(vertx);
  }

  public Router getRouter() {
    return router;
  }
}
