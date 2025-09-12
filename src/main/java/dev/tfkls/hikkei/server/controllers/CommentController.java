package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.ParamFlow;
import dev.tfkls.hikkei.server.util.Type;
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
    ParamFlow.handle(
        ctx,
        Map.of("page_id", Type.Int, "content", Type.Str),
        null,
        body -> {
          Integer id = body.getInteger("page_id");
          Integer userId = ctx.get("id");
          String content = body.getString("content");

          int idc = commentProvider.addComment(new CommentData(id, userId, content));
          ctx.response().setStatusCode(201);
          ctx.json(new JsonObject().put("id", idc));
        });
  }

  public void deleteComment(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("id", Type.Int),
        null,
        body -> {
          commentProvider.deleteComment(body.getInteger("id"));
          ctx.response().setStatusCode(204).end();
        });
  }

  private void getList(RoutingContext ctx, String type) {
    ParamFlow.handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Integer id = body.getInteger("id");

          List<CommentData> data =
              type.equals("page") ? commentProvider.getByPage(id) : commentProvider.getByUser(id);
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          JsonArray items = new JsonArray();
          for (CommentData item : data) items.add(Jsonify.commentToJson(item));

          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void getByPage(RoutingContext ctx) {
    getList(ctx, "page");
  }

  public void getByUser(RoutingContext ctx) {
    getList(ctx, "user");
  }

  public void getById(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          CommentData data = commentProvider.getById(body.getInteger("id"));
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          ctx.response().setStatusCode(200);
          ctx.json(Jsonify.commentToJson(data));
        });
  }
}
