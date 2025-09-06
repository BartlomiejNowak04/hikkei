package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.*;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;

public class CategoryController {
  private final CategoryDataProvider categoryProvider;

  public CategoryController(CategoryDataProvider categoryProvider) {
    this.categoryProvider = categoryProvider;
  }

  public void getCategories(RoutingContext ctx) {
    handle(
        ctx,
        Map.of(),
        body -> {
          List<CategoryData> data = categoryProvider.getCategories();
          JsonArray items = new JsonArray();
          for (CategoryData item : data) items.add(categoryToJson(item));
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void getById(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class),
        body -> {
          CategoryData data = categoryProvider.getById(body.getInteger("id"));
          if (data == null) throw new BadDataException("Invalid parameter: id");
          else if (data == CategoryData.DELETED) ctx.response().setStatusCode(404).end();
          else {
            ctx.response().setStatusCode(200);
            ctx.json(categoryToJson(data));
          }
        });
  }

  public void modifyPermission(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("category", Integer.class, "user", Integer.class, "permission", String.class),
        body -> {
          categoryProvider.updatePermission(
              body.getInteger("category"),
              body.getInteger("user"),
              PermissionLevel.valueOf(body.getString("permission")));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void createPermission(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("name", String.class),
        body -> {
          categoryProvider.createCategory(new CategoryData(body.getString("name")));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void updateName(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class, "name", String.class),
        body -> {
          categoryProvider.changeName(body.getInteger("id"), body.getString("name"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteCategory(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class),
        body -> {
          categoryProvider.deleteCategory(body.getInteger("id"));
        });
  }
}
