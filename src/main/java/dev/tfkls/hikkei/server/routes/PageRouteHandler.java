package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.server.controllers.PageController;
import dev.tfkls.hikkei.server.handlers.ArgumentCheckHandler;
import dev.tfkls.hikkei.server.handlers.JsonHeaderHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Map;

public class PageRouteHandler extends RouteHandler {
  public PageRouteHandler(
      Vertx vertx, PageDataProvider pageProvider, VersionProvider versionProvider) {
    super(vertx);
    PageController controller = new PageController(pageProvider, versionProvider);

    router.route("/*").handler(BodyHandler.create());
    router.route("/*").handler(new JsonHeaderHandler());

    router.get("/random").handler(controller::getRandomPage);

    router.post("/search").handler(new ArgumentCheckHandler(Map.of("title", String.class)));
    router.post("/search").handler(controller::searchByTitle);

    router.post("/revisions").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post("/revisions").handler(controller::getRevisions);

    router.post("/current").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post("/current").handler(controller::getCurrentContent);

    router.post("/comments").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post("/comments").handler(controller::getComments);

    router
        .post("/get")
        .handler(new ArgumentCheckHandler(Map.of("key", String.class, "url", Boolean.class)));
    router.post("/get").handler(controller::getPageByTitle);

    router
        .put("/create")
        .handler(
            new ArgumentCheckHandler(Map.of("title", String.class, "category", Integer.class)));
    router.put("/create").handler(controller::createPage);

    router
        .post("/update_title")
        .handler(new ArgumentCheckHandler(Map.of("title", String.class, "id", Integer.class)));
    router.post("/update_title").handler(controller::updateTitle);

    router
        .post("/update_category")
        .handler(new ArgumentCheckHandler(Map.of("category", Integer.class, "id", Integer.class)));
    router.post("/update_category").handler(controller::updateCategory);

    router
        .post("/archive")
        .handler(new ArgumentCheckHandler(Map.of("value", Boolean.class, "id", Integer.class)));
    router.post("/archive").handler(controller::toggleArchive);
  }
}
