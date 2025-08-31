package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.controllers.UserController;
import io.vertx.core.Vertx;

public class UserRouteHandler extends RouteHandler {
  public UserRouteHandler(Vertx vertx, UserDataProvider userProvider) {
    super(vertx);
    UserController controller = new UserController(userProvider);
  }
}
