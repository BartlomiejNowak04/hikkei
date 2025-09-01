package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.server.controllers.CategoryController;
import io.vertx.core.Vertx;

public class CategoryRouteHandler extends RouteHandler {
  public CategoryRouteHandler(Vertx vertx, CategoryDataProvider categoryProvider) {
    super(vertx);
    CategoryController controller = new CategoryController(categoryProvider);

    //    router.post("/").blockingHandler(null);
    //    router.post("/byid").blockingHandler(null);
    //    router.post("/permission").blockingHandler(null);
    //
    //    router.put("/create").blockingHandler(null);
    //    router.put("/update/name").blockingHandler(null);
  }
}
