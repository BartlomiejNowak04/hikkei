package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
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
    handle(
        ctx,
        null,
        body -> {
          PageData data = pageProvider.getRandomPage();
          if (data == null) throw new InternalErrorException("No pages available");
          ctx.response().setStatusCode(200);
          ctx.json(Utils.pageToJson(data));
        });
  }

  public void searchByTitle(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("title", String.class),
        body -> {
          List<PageData> data = pageProvider.searchByTitle(body.getString("title"));
          JsonArray items = new JsonArray();
          for (PageData item : data) items.add(Utils.pageToJson(item));
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void getPageByTitle(RoutingContext ctx, boolean fromUrl) {
    handle(
        ctx,
        Map.of(fromUrl ? "url" : "title", String.class),
        body -> {
          String title = body.getString(fromUrl ? "url" : "title");
          if (fromUrl) title = title.replace("_", " ");

          PageData data = pageProvider.getByTitle(title);
          if (data == null) throw new BadDataException("Invalid page title: " + title);
          ctx.response().setStatusCode(200);
          ctx.json(Utils.pageToJson(data));
        });
  }

  public void getByCategory(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("category", Integer.class),
        body -> {
          List<PageData> data = pageProvider.getByCategory(body.getInteger("category"));
          if (data == null) throw new BadDataException("Invalid category id");
          JsonArray items = new JsonArray();
          for (PageData item : data) items.add(Utils.pageToJson(item));
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void createPage(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("title", String.class, "category", Integer.class, "user", Integer.class),
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
    handle(
        ctx,
        Map.of("title", String.class, "id", Integer.class),
        body -> {
          pageProvider.updatePageTitle(body.getInteger("id"), body.getString("title"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void updateCategory(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("category", Integer.class, "id", Integer.class),
        body -> {
          pageProvider.updatePageCategory(body.getInteger("id"), body.getInteger("category"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void toggleArchive(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("value", Boolean.class, "id", Integer.class),
        body -> {
          pageProvider.toggleArchive(body.getInteger("id"), body.getBoolean("value"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deletePage(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class),
        body -> {
          pageProvider.deletePage(body.getInteger("id"));
          ctx.response().setStatusCode(204).end();
        });
  }
}
