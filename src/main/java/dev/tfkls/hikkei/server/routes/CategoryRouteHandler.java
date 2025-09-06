package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.controllers.CategoryController;
import io.vertx.core.Vertx;

public class CategoryRouteHandler extends RouteHandler {
  public CategoryRouteHandler(
      Vertx vertx, CategoryDataProvider categoryProvider, UserDataProvider userProvider) {
    super(vertx);
    CategoryController controller = new CategoryController(categoryProvider, userProvider);

    router.post("/").blockingHandler(controller::getCategories);
    router.post("/byid").blockingHandler(controller::getById);
    router.post("/users").blockingHandler(controller::getUsersWithPermissions);

    router.put("/permission").blockingHandler(controller::modifyPermission);
    router.put("/create").blockingHandler(controller::createPermission);
    router.put("/update/name").blockingHandler(controller::updateName);

    router.delete("/delete").blockingHandler(controller::deleteCategory);
  }
}
