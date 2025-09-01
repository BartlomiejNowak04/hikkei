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

    router.post("/user").blockingHandler(controller::getByUser);
    router.post("/page").blockingHandler(controller::getByPage);
    //    router.post("/render").blockingHandler(controller::Render);
    router.post("/content").blockingHandler(controller::getCurrentContent);
    router.post("/byid").blockingHandler(controller::getById);
    //    router.post("/draft").blockingHandler(null);

    router.put("/create").blockingHandler(controller::addRevision);
    router.put("/update/status").blockingHandler(controller::changeStatus);
    //    router.put("/draft/save").blockingHandler(null);

    //    router.delete("/draft/delete").blockingHandler(null);
  }
}
