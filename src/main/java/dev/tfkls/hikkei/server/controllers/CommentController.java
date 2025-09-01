package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.*;

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

  private void getList(RoutingContext ctx, String type) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of(type, Integer.class));
      Integer id = body.getInteger(type);

      List<CommentData> data =
          type.equals("page") ? commentProvider.getByPage(id) : commentProvider.getByUser(id);
      if (data == null) throw new BadDataException("Incorrect parameter value: id");
      JsonArray items = new JsonArray();
      for (CommentData item : data) items.add(Utils.commentToJson(item));

      ctx.response().setStatusCode(200);
      ctx.json(items);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getByPage(RoutingContext ctx) {
    getList(ctx, "page");
  }

  public void getByUser(RoutingContext ctx) {
    getList(ctx, "user");
  }

  public void getById(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class, "page_id", Integer.class));
      Integer id = body.getInteger("id");
      Integer pageId = body.getInteger("page_id");

      CommentData data = commentProvider.getById(pageId, id);
      if (data == null) throw new BadDataException("Incorrect parameter value: id");
      ctx.response().setStatusCode(200);
      ctx.json(commentToJson(data));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
