package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.server.controllers.PageController;
import io.vertx.core.Vertx;

public class PageRouteHandler extends RouteHandler {
  public PageRouteHandler(Vertx vertx, PageDataProvider pageProvider) {
    super(vertx);
    PageController controller = new PageController(pageProvider);

    router.post("/random").blockingHandler(controller::getRandomPage);
    router.post("/bytitle").blockingHandler(controller::getPageByTitle);
    router.post("byurl").blockingHandler(null);
    router.post("/search").blockingHandler(controller::searchByTitle);
    router.post("/category").blockingHandler(null);

    router.put("/create").blockingHandler(controller::createPage);
    router.put("/update/title").blockingHandler(controller::updateTitle);
    router.put("/update/category").blockingHandler(controller::updateCategory);
    router.put("/update/archive").blockingHandler(controller::toggleArchive);
  }
}
