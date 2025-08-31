package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.server.controllers.RevisionController;
import dev.tfkls.hikkei.server.handlers.ArgumentCheckHandler;
import dev.tfkls.hikkei.server.handlers.JsonHeaderHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Map;

public class RevisionRouteHandler extends RouteHandler {
  public RevisionRouteHandler(
      Vertx vertx, RevisionDataProvider revisionProvider, VersionProvider versionProvider) {
    super(vertx);
    RevisionController controller = new RevisionController(revisionProvider, versionProvider);

    router.route("/*").handler(BodyHandler.create());
    router.route("/*").handler(new JsonHeaderHandler());

    router
        .put("/create")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put("/create").handler(controller::addRevision);

    router
        .post("/status")
        .handler(
            new ArgumentCheckHandler(
                Map.of("id", Integer.class, "status", Integer.class, "page_id", Integer.class)));
    router.post("/status").handler(controller::changeStatus);
  }
}
