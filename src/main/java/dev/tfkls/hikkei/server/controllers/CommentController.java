package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;
import static dev.tfkls.hikkei.server.utils.ResponseCloser.endWithStatus;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class CommentController {
  private final CommentDataProvider commentProvider;

  public CommentController(CommentDataProvider commentProvider) {
    this.commentProvider = commentProvider;
  }

  public void deleteComment(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    Integer pageId = body.getInteger("page_id");
    try {
      commentProvider.deleteComment(pageId, id);
      endWithStatus(200, response);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void addComment(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    String content = body.getString("content");
    try {
      commentProvider.addComment(new CommentData(null, id, content, null, null));
      endWithStatus(201, response);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
