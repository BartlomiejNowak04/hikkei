package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.util.AccessManager;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.ParamFlow;
import dev.tfkls.hikkei.server.util.Type;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.List;
import java.util.Map;

public class CategoryController {
  private final CategoryDataProvider categoryProvider;
  private final UserDataProvider userProvider;

  public CategoryController(CategoryDataProvider categoryProvider, UserDataProvider userProvider) {
    this.categoryProvider = categoryProvider;
    this.userProvider = userProvider;
  }

  public void getCategories(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        null,
        null,
        body -> {
          List<CategoryData> data = categoryProvider.getCategories();
          JsonArray items = new JsonArray();
          for (CategoryData item : data) items.add(Jsonify.categoryToJson(item));
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void getById(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          CategoryData data = categoryProvider.getById(body.getInteger("id"));
          if (data == null) throw new BadDataException("Invalid parameter: id");
          else if (data == CategoryData.DELETED) ctx.response().setStatusCode(404).end();
          else {
            ctx.response().setStatusCode(200);
            ctx.json(Jsonify.categoryToJson(data));
          }
        });
  }

  public void getUsersWithPermissions(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Map<Integer, PermissionLevel> vals =
              categoryProvider.getUsersPermissions(body.getInteger("id"));
          if (vals == null) throw new BadDataException("Invalid parameter: id");
          JsonArray items = new JsonArray();
          for (Map.Entry<Integer, PermissionLevel> val : vals.entrySet()) {
            UserData data = userProvider.getUserData(val.getKey());
            if (data == null || data == UserData.DELETED) continue;
            items.add(Jsonify.userToJson(data).put("permission", val.getValue().toString()));
          }
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void modifyPermission(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("category", Type.Int, "user", Type.Int, "permission", Type.Str),
        null,
        body -> {
          PermissionLevel permission = null;
          int categoryId = body.getInteger("category");
          int userId = body.getInteger("user");

          if (!body.getString("permission").equals("null")) {
            try {
              permission = PermissionLevel.valueOf(body.getString("permission"));
            } catch (IllegalArgumentException e) {
              throw new BadDataException("Invalid permission type");
            }
          }

          PermissionLevel oldPermission =
              categoryProvider
                  .getUserPermission(categoryId, userId)
                  .orElse(categoryProvider.getById(categoryId).defaultPermission());

          AccessManager.setCategory(ctx, categoryId);
          AccessManager.verifyPermission(
              ctx,
              PermissionLevel.successor(
                  PermissionLevel.max(
                      permission != null ? permission : PermissionLevel.NO_ACCESS, oldPermission)));

          categoryProvider.updateUserPermission(categoryId, userId, permission);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void createCategory(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("name", Type.Str),
        null,
        body -> {
          AccessManager.verifyPermission(ctx, PermissionLevel.ADMIN);

          int idc = categoryProvider.createCategory(new CategoryData(body.getString("name")));
          ctx.response().setStatusCode(201);
          ctx.json(new JsonObject().put("id", idc));
        });
  }

  public void updateName(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("id", Type.Int, "name", Type.Str),
        null,
        body -> {
          AccessManager.verifyPermission(ctx, PermissionLevel.ADMIN);
          categoryProvider.changeName(body.getInteger("id"), body.getString("name"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void updateDefaultPermission(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("id", Type.Int, "permission", Type.Str),
        null,
        body -> {
          PermissionLevel permission;
          try {
            permission = PermissionLevel.valueOf(body.getString("permission"));
          } catch (IllegalArgumentException e) {
            throw new HttpException(400, "Invalid permission type");
          }
          AccessManager.verifyPermission(ctx, PermissionLevel.ADMIN);
          categoryProvider.updateDefaultPermission(body.getInteger("id"), permission);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteCategory(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("id", Type.Int),
        null,
        body -> {
          AccessManager.verifyPermission(ctx, PermissionLevel.ADMIN);

          categoryProvider.deleteCategory(body.getInteger("id"));
          ctx.response().setStatusCode(204).end();
        });
  }
}
