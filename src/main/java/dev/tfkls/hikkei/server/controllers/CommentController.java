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
    handle(
        ctx,
        Map.of("id", Integer.class, "user", Integer.class, "content", String.class),
        body -> {
          Integer id = body.getInteger("id");
          Integer userId = body.getInteger("user");
          String content = body.getString("content");

          int idc = commentProvider.addComment(new CommentData(id, userId, content));
          ctx.response().setStatusCode(201);
          ctx.json(new JsonObject().put("id", idc));
        });
  }

  public void deleteComment(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class),
        body -> {
          commentProvider.deleteComment(body.getInteger("id"));
          ctx.response().setStatusCode(200).end();
        });
  }

  private void getList(RoutingContext ctx, String type) {
    handle(
        ctx,
        Map.of(type, Integer.class),
        body -> {
          Integer id = body.getInteger(type);

          List<CommentData> data =
              type.equals("page") ? commentProvider.getByPage(id) : commentProvider.getByUser(id);
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          JsonArray items = new JsonArray();
          for (CommentData item : data) items.add(Utils.commentToJson(item));

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
    handle(
        ctx,
        Map.of("id", Integer.class),
        body -> {
          CommentData data = commentProvider.getById(body.getInteger("id"));
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          ctx.response().setStatusCode(200);
          ctx.json(commentToJson(data));
        });
  }
}
