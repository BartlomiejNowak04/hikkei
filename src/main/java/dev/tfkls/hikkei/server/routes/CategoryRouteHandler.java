package dev.tfkls.hikkei.server.routes;

import io.vertx.core.Vertx;

public class CategoryRouteHandler extends RouteHandler {
  public CategoryRouteHandler(Vertx vertx) {
    super(vertx);

    router.post("/").blockingHandler(null);
    router.post("/byid").blockingHandler(null);
    router.post("/permission").blockingHandler(null);

    router.put("/create").blockingHandler(null);
    router.put("/update/name").blockingHandler(null);
  }
}
