package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.controllers.UserController;
import io.vertx.core.Vertx;

public class UserRouteHandler extends RouteHandler {
  public UserRouteHandler(
      Vertx vertx,
      UserDataProvider userProvider,
      FileProvider fileProvider,
      CategoryDataProvider categoryProvider) {
    super(vertx);
    UserController controller = new UserController(userProvider, fileProvider, categoryProvider);

    //    router.post("/login").blockingHandler(null);
    router.post("/byid").blockingHandler(controller::getUser);
    router.post("/pfp").blockingHandler(controller::getPfp);
    router.post("/categories").blockingHandler(controller::getCategoriesWithPermissions);

    router.put("/create").blockingHandler(controller::createUser);
    router.put("/update/name").blockingHandler(controller::updateName);
    router.put("/update/pfp").blockingHandler(controller::updatePfp);

    router.delete("/delete").blockingHandler(controller::deleteUser);
  }
}
