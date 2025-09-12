package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.ParamFlow;
import dev.tfkls.hikkei.server.util.Type;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
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
          if (!body.getString("permission").equals("null")) {
            try {
              permission = PermissionLevel.valueOf(body.getString("permission"));
            } catch (IllegalArgumentException e) {
              throw new BadDataException("Invalid permission type");
            }
          }

          categoryProvider.updatePermission(
              body.getInteger("category"), body.getInteger("user"), permission);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void createCategory(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("name", Type.Str),
        null,
        body -> {
          categoryProvider.createCategory(new CategoryData(body.getString("name")));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void updateName(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("id", Type.Int, "name", Type.Str),
        null,
        body -> {
          categoryProvider.changeName(body.getInteger("id"), body.getString("name"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteCategory(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("id", Type.Int),
        null,
        body -> {
          categoryProvider.deleteCategory(body.getInteger("id"));
        });
  }
}
