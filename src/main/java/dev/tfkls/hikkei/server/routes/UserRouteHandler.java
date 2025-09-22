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

    router.get("/:id").blockingHandler(controller::getUser);
    router.get("/pfp/:id").blockingHandler(controller::getPfp);
    router.get("/categories/:id").blockingHandler(controller::getCategoriesWithPermissions);

    router.post("/create").blockingHandler(controller::createUser);
    router.put("/update/name").blockingHandler(controller::updateName);
    router.put("/update/pfp").blockingHandler(controller::updatePfp);
    router.put("/update/permission").blockingHandler(controller::updatePermission);

    router.delete("/delete").blockingHandler(controller::deleteUser);
  }
}
