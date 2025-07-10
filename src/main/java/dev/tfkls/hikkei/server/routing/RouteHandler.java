package dev.tfkls.hikkei.server.routing;

import io.vertx.ext.web.Router;

public interface RouteHandler {
  void addRoutes(Router router, String url);
}
