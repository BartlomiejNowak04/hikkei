package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.controllers.UserController;
import io.vertx.core.Vertx;

public class UserRouteHandler extends RouteHandler {
  public UserRouteHandler(Vertx vertx, UserDataProvider userProvider) {
    super(vertx);
    UserController controller = new UserController(userProvider);

    //    router.post("/login").blockingHandler(null);
    //    router.post("/byid").blockingHandler(null);
    //    router.post("/role").blockingHandler(null);
    //
    //    router.put("/create").blockingHandler(null);
    //    router.put("/update/name").blockingHandler(null);
    //    router.put("/update/pfp").blockingHandler(null);
    //    router.put("/update/role").blockingHandler(null);
    //
    //    router.delete("/delete").blockingHandler(null);
  }
}
