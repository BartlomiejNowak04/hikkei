package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.ParamFlow;
import dev.tfkls.hikkei.server.util.Type;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class CommentController {
  private final CommentDataProvider commentProvider;
  private final PageDataProvider pageProvider;
  private final AccessManager accessManager;

  public CommentController(
      CommentDataProvider commentProvider,
      PageDataProvider pageProvider,
      AccessManager accessManager) {
    this.commentProvider = commentProvider;
    this.pageProvider = pageProvider;
    this.accessManager = accessManager;
  }

  public void addComment(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("page_id", Type.Int, "content", Type.Str),
        null,
        body -> {
          Integer id = body.getInteger("page_id");
          Integer userId = accessManager.requireUser(ctx);
          String content = body.getString("content");

          accessManager.setCategory(ctx, pageProvider.getById(id).category());
          accessManager.verifyPermission(ctx, PermissionLevel.UNTRUSTED);

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
          OptionalInt maybeUserId = accessManager.fetchUser(ctx);
          CommentData commentData = commentProvider.getById(body.getInteger("id"));

          if (maybeUserId.isEmpty() || maybeUserId.getAsInt() != commentData.userId()) {

            accessManager.setCategory(ctx, pageProvider.getById(commentData.pageId()).category());
            accessManager.verifyPermission(ctx, PermissionLevel.UNTRUSTED);
          }

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

          List<CommentData> data;
          if (type.equals("page")) {
            int pageCategory = pageProvider.getById(id).category();
            accessManager.setCategory(ctx, pageCategory);
            accessManager.verifyPermission(ctx, PermissionLevel.UNTRUSTED);

            data = commentProvider.getByPage(id);
          } else if (type.equals("user")) {
            OptionalInt userId = accessManager.fetchUser(ctx);
            // Only global moderators can see other users' comments
            if (userId.isEmpty() || userId.getAsInt() != id) {
              accessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);
            }

            data = commentProvider.getByUser(id);
          } else {
            throw new IllegalArgumentException("Expected 'page' or 'user' as the type");
          }

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

          accessManager.setCategory(ctx, pageProvider.getById(data.pageId()).category());
          accessManager.verifyPermission(ctx, PermissionLevel.UNTRUSTED);

          ctx.response().setStatusCode(200);
          ctx.json(Jsonify.commentToJson(data));
        });
  }
}
