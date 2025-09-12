package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.server.controllers.CommentController;
import io.vertx.core.Vertx;

public class CommentRouteHandler extends RouteHandler {
  public CommentRouteHandler(Vertx vertx, CommentDataProvider commentProvider) {
    super(vertx);
    CommentController controller = new CommentController(commentProvider);

    router.get("/user/:id").blockingHandler(controller::getByUser);
    router.get("/page/:id").blockingHandler(controller::getByPage);
    router.get("/:id").blockingHandler(controller::getById);

    router.post("/create").blockingHandler(controller::addComment);

    router.delete("/delete").blockingHandler(controller::deleteComment);
  }
}
