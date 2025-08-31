package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.server.controllers.CommentController;
import dev.tfkls.hikkei.server.handlers.ArgumentCheckHandler;
import dev.tfkls.hikkei.server.handlers.JsonHeaderHandler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Map;

public class CommentRouteHandler extends RouteHandler {
  public CommentRouteHandler(Vertx vertx, CommentDataProvider commentProvider) {
    super(vertx);
    CommentController controller = new CommentController(commentProvider);

    router.route("/*").handler(BodyHandler.create());
    router.route("/*").handler(new JsonHeaderHandler());

    router
        .put("/add")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put("/add").handler(controller::addComment);

    router
        .post("/delete")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "page_id", Integer.class)));
    router.post("/delete").handler(controller::deleteComment);
  }
}
