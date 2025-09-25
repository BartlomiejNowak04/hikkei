package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.server.controllers.PageController;
import io.vertx.core.Vertx;

public class PageRouteHandler extends RouteHandler {
  public PageRouteHandler(Vertx vertx, PageDataProvider pageProvider, AccessManager accessManager) {
    super(vertx);
    PageController controller = new PageController(pageProvider, accessManager);

    router.get("/random").blockingHandler(controller::getRandomPage);
    router.get("/title/:title").blockingHandler(ctx -> controller.getPageByTitle(ctx, false));
    router.get("/:url").blockingHandler(ctx -> controller.getPageByTitle(ctx, true));
    router.get("/search/:title").blockingHandler(controller::searchByTitle);
    router.get("/category/:id").blockingHandler(controller::getByCategory);

    router.post("/create").blockingHandler(controller::createPage);
    router.put("/update/title").blockingHandler(controller::updateTitle);
    router.put("/update/category").blockingHandler(controller::updateCategory);
    router.put("/update/archive").blockingHandler(controller::toggleArchive);

    router.delete("/delete").blockingHandler(controller::deletePage);
  }
}
