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

    router.post("/random").handler(controller::getRandomPage);
    router.post("/search").handler(controller::searchByTitle);
    router.post("/revisions").handler(controller::getRevisions);
    router.post("/content").handler(controller::getCurrentContent);
    router.post("/comments").handler(controller::getComments);
    router.post("/from_title").handler(controller::getPageByTitle);
    router.put("/create").handler(controller::createPage);
    router.put("/update/title").handler(controller::updateTitle);
    router.put("/update/category").handler(controller::updateCategory);
    router.post("/archive").handler(controller::toggleArchive);
  }
}
