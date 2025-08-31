package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import static dev.tfkls.hikkei.server.util.ResponseCloser.endWithStatus;
import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

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
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    } catch (BadDataException e) {
      throwError(400, e.getMessage(), response);
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
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    } catch (BadDataException e) {
      throwError(400, e.getMessage(), response);
    }
  }
}
