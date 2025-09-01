package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.server.controllers.RevisionController;
import io.vertx.core.Vertx;

public class RevisionRouteHandler extends RouteHandler {
  public RevisionRouteHandler(
      Vertx vertx, RevisionDataProvider revisionProvider, VersionProvider versionProvider) {
    super(vertx);
    RevisionController controller = new RevisionController(revisionProvider, versionProvider);

    router.post("/user").blockingHandler(null);
    router.post("/page").blockingHandler(controller::getByPage);
    router.post("/render").blockingHandler(null);
    router.post("/content").blockingHandler(controller::getCurrentContent);
    router.post("/byid").blockingHandler(null);

    router.put("/create").blockingHandler(controller::addRevision);
    router.put("/update/status").blockingHandler(controller::changeStatus);
    router.put("/draft/save").blockingHandler(null);

    router.delete("/draft/delete").blockingHandler(null);
  }
}
