package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.checkBody;
import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;

public class CommentController {
  private final CommentDataProvider commentProvider;

  public CommentController(CommentDataProvider commentProvider) {
    this.commentProvider = commentProvider;
  }

  public void addComment(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class, "content", String.class));
      Integer id = body.getInteger("id");
      String content = body.getString("content");

      int idc = commentProvider.addComment(new CommentData(null, id, content, null, null));
      ctx.response().setStatusCode(201);
      ctx.json(new JsonObject().put("id", idc));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void deleteComment(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class, "page_id", Integer.class));
      Integer id = body.getInteger("id");
      Integer pageId = body.getInteger("page_id");

      commentProvider.deleteComment(pageId, id);
      ctx.response().setStatusCode(200).end();
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getByPage(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class));
      Integer id = body.getInteger("id");

      List<CommentData> data = commentProvider.getByPage(id);
      if (data == null) throw new BadDataException("Incorrect parameter value: id");
      JsonArray items = new JsonArray();
      for (CommentData item : data) items.add(Utils.commentToJson(item));

      ctx.response().setStatusCode(200);
      ctx.json(items);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
