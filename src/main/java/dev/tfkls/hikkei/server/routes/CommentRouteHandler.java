package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.server.controllers.CommentController;
import io.vertx.core.Vertx;

public class CommentRouteHandler extends RouteHandler {
  public CommentRouteHandler(Vertx vertx, CommentDataProvider commentProvider) {
    super(vertx);
    CommentController controller = new CommentController(commentProvider);

    router.post("/user").blockingHandler(null);
    router.post("/page").blockingHandler(controller::getByPage);
    router.post("/byid").blockingHandler(null);

    router.put("/create").blockingHandler(controller::addComment);

    router.delete("/delete").blockingHandler(controller::deleteComment);
  }
}
