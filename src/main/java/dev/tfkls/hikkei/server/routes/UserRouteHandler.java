package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.controllers.UserController;
import io.vertx.core.Vertx;

public class UserRouteHandler extends RouteHandler {
  public UserRouteHandler(Vertx vertx, UserDataProvider userProvider, FileProvider fileProvider) {
    super(vertx);
    UserController controller = new UserController(userProvider, fileProvider);

    //    router.post("/login").blockingHandler(null);
    //    router.post("/byid").blockingHandler(null);
    router.post("/pfp").blockingHandler(controller::getPfp);
    //
    //    router.put("/create").blockingHandler(null);
    //    router.put("/update/name").blockingHandler(null);
    router.put("/update/pfp").blockingHandler(controller::updatePfp);
    //
    //    router.delete("/delete").blockingHandler(null);
  }
}
