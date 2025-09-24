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

    router.get("/all").blockingHandler(controller::getCategories);
    router.get("/:id").blockingHandler(controller::getById);
    router.get("/users/:id").blockingHandler(controller::getUsersWithPermissions);

    router.put("/permission").blockingHandler(controller::modifyPermission);
    router.post("/create").blockingHandler(controller::createCategory);
    router.put("/update/name").blockingHandler(controller::updateName);
    router.put("/update/permission").blockingHandler(controller::updateDefaultPermission);

    router.delete("/delete").blockingHandler(controller::deleteCategory);
  }
}
