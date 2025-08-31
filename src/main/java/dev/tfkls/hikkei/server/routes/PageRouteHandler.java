package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.server.controllers.PageController;
import io.vertx.core.Vertx;

public class PageRouteHandler extends RouteHandler {
  public PageRouteHandler(
      Vertx vertx, PageDataProvider pageProvider, VersionProvider versionProvider) {
    super(vertx);
    PageController controller = new PageController(pageProvider, versionProvider);

    router.post("/random").blockingHandler(controller::getRandomPage);
    router.post("/search").blockingHandler(controller::searchByTitle);
    router.post("/revisions").blockingHandler(controller::getRevisions);
    router.post("/content").blockingHandler(controller::getCurrentContent);
    router.post("/comments").blockingHandler(controller::getComments);
    router.post("/from_title").blockingHandler(controller::getPageByTitle);
    router.put("/create").blockingHandler(controller::createPage);
    router.put("/update/title").blockingHandler(controller::updateTitle);
    router.put("/update/category").blockingHandler(controller::updateCategory);
    router.post("/archive").blockingHandler(controller::toggleArchive);
  }
}
