package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.ParamFlow;
import dev.tfkls.hikkei.server.util.Type;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;

public class PageController {
  private final PageDataProvider pageProvider;

  public PageController(PageDataProvider pageProvider) {
    this.pageProvider = pageProvider;
  }

  public void getRandomPage(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        null,
        null,
        body -> {
          PageData data = pageProvider.getRandomPage();
          if (data == null) throw new InternalErrorException("No pages available");
          ctx.response().setStatusCode(200);
          ctx.json(Jsonify.pageToJson(data));
        });
  }

  public void searchByTitle(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        null,
        Map.of("title", Type.Str),
        body -> {
          List<PageData> data = pageProvider.searchByTitle(body.getString("title"));
          JsonArray items = new JsonArray();
          for (PageData item : data) items.add(Jsonify.pageToJson(item));
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void getPageByTitle(RoutingContext ctx, boolean fromUrl) {
    ParamFlow.handle(
        ctx,
        null,
        Map.of(fromUrl ? "url" : "title", Type.Str),
        body -> {
          String title = body.getString(fromUrl ? "url" : "title");
          if (fromUrl) title = title.replace("_", " ");

          PageData data = pageProvider.getByTitle(title);
          if (data == null) throw new BadDataException("Invalid page title: " + title);
          ctx.response().setStatusCode(200);
          ctx.json(Jsonify.pageToJson(data));
        });
  }

  public void getByCategory(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          List<PageData> data = pageProvider.getByCategory(body.getInteger("id"));
          if (data == null) throw new BadDataException("Invalid category id");
          JsonArray items = new JsonArray();
          for (PageData item : data) items.add(Jsonify.pageToJson(item));
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void createPage(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("title", Type.Str, "category", Type.Str),
        null,
        body -> {
          String title = body.getString("title");
          Integer category = body.getInteger("category");
          Integer user = body.getInteger("user");

          int id = pageProvider.createPage(new PageData(user, title, category));
          ctx.response().setStatusCode(201);
          ctx.json(new JsonObject().put("id", id));
        });
  }

  public void updateTitle(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("title", Type.Str, "id", Type.Int),
        null,
        body -> {
          pageProvider.updatePageTitle(body.getInteger("id"), body.getString("title"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void updateCategory(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("category", Type.Int, "id", Type.Int),
        null,
        body -> {
          pageProvider.updatePageCategory(body.getInteger("id"), body.getInteger("category"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void toggleArchive(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("value", Type.Bool, "id", Type.Int),
        null,
        body -> {
          pageProvider.toggleArchive(body.getInteger("id"), body.getBoolean("value"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deletePage(RoutingContext ctx) {
    ParamFlow.handle(
        ctx,
        Map.of("id", Type.Int),
        null,
        body -> {
          pageProvider.deletePage(body.getInteger("id"));
          ctx.response().setStatusCode(204).end();
        });
  }
}
