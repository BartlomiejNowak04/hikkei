package dev.tfkls.hikkei.server.routing;

import dev.tfkls.hikkei.model.user.UserManager;
import io.vertx.ext.web.Router;

public class UserRouteHandler implements RouteHandler {
  private final UserManager userManager;

  public UserRouteHandler(UserManager manager) {
    userManager = manager;
  }

  @Override
  public void addRoutes(Router router, String url) {}
}
