package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.server.controllers.RevisionController;
import io.vertx.core.Vertx;

public class RevisionRouteHandler extends RouteHandler {
  public RevisionRouteHandler(
      Vertx vertx, RevisionDataProvider revisionProvider, VersionProvider versionProvider) {
    super(vertx);
    RevisionController controller = new RevisionController(revisionProvider, versionProvider);

    router.get("/user/:id").blockingHandler(controller::getByUser);
    router.get("/page/:id").blockingHandler(controller::getByPage);
    //    router.post("/render").blockingHandler(controller::Render);
    router.get("/content/:id").blockingHandler(controller::getCurrentContent);
    router.get("/:id").blockingHandler(controller::getById);
    router.get("/draft/:page_id").blockingHandler(controller::getDraft);

    router.post("/create").blockingHandler(controller::addRevision);
    router.put("/update/status").blockingHandler(controller::changeStatus);
    router.post("/draft/save").blockingHandler(controller::saveDraft);

    router.delete("/draft/delete").blockingHandler(controller::deleteDraft);
    router.delete("/delete").blockingHandler(controller::deleteRevision);
  }
}
