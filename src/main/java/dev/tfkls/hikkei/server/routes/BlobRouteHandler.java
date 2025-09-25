package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.blob.BlobDataProvider;
import dev.tfkls.hikkei.server.controllers.BlobController;
import io.vertx.core.Vertx;

public class BlobRouteHandler extends RouteHandler {

  public BlobRouteHandler(Vertx vertx, BlobDataProvider blobProvider, AccessManager accessManager) {
    super(vertx);
    BlobController blobController = new BlobController(blobProvider, accessManager);

    router.get("/user/:id").blockingHandler(blobController::getUserBlobs);
    router.post("/create").blockingHandler(blobController::createBlob);
    router.get("/:id/data").blockingHandler(blobController::getBlobData);
    router.get("/:id/meta").blockingHandler(blobController::getBlobMetadata);
    router.delete("/delete").blockingHandler(blobController::deleteBlob);
  }
}
