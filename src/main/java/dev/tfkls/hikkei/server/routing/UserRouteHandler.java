package dev.tfkls.hikkei.server.routing;

import dev.tfkls.hikkei.model.user.UserDataProvider;
import io.vertx.ext.web.Router;

public class UserRouteHandler implements RouteHandler {
  private final UserDataProvider userProvider;

  public UserRouteHandler(UserDataProvider userProvider) {
    this.userProvider = userProvider;
  }

  @Override
  public void addRoutes(Router router, String url) {}
}
