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

    router.put("/create").blockingHandler(controller::addRevision);
    router.put("/status").blockingHandler(controller::changeStatus);
  }
}
