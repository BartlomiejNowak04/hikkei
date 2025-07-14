package dev.tfkls.hikkei.server.routing;

import static dev.tfkls.hikkei.server.util.ResponseCloser.endWithStatus;
import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.server.routing.handler.ArgumentCheckHandler;
import dev.tfkls.hikkei.server.routing.handler.JsonHeaderHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Map;

public class CommentRouteHandler implements RouteHandler {
  private final CommentDataProvider commentProvider;

  public CommentRouteHandler(CommentDataProvider commentProvider) {
    this.commentProvider = commentProvider;
  }

  private void deleteComment(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    Integer pageId = body.getInteger("page_id");
    try {
      commentProvider.deleteComment(pageId, id);
      endWithStatus(200, response);
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    } catch (BadDataException e) {
      throwError(400, e.getMessage(), response);
    }
  }

  private void addComment(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    String content = body.getString("content");
    try {
      commentProvider.addComment(new CommentData(null, id, content, null, null));
      endWithStatus(201, response);
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    } catch (BadDataException e) {
      throwError(400, e.getMessage(), response);
    }
  }

  @Override
  public void addRoutes(Router router, String url) {
    router.route(url + "/*").handler(BodyHandler.create());
    router.route(url + "/*").handler(new JsonHeaderHandler());

    router
        .put(url + "/add")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put(url + "/add").handler(this::addComment);

    router
        .post(url + "/delete")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "page_id", Integer.class)));
    router.post(url + "/delete").handler(this::deleteComment);
  }
}
