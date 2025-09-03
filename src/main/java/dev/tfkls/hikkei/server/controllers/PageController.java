package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.checkBody;
import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;

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
    try {
      PageData data = pageProvider.getRandomPage();
      if (data == null) throw new InternalErrorException("No pages available");
      ctx.response().setStatusCode(200);
      ctx.json(Utils.pageToJson(data));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void searchByTitle(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("title", String.class));
      String title = body.getString("title");

      List<PageData> data = pageProvider.searchByTitle(title);
      JsonArray items = new JsonArray();
      for (PageData item : data) items.add(Utils.pageToJson(item));
      ctx.response().setStatusCode(200);
      ctx.json(items);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getPageByTitle(RoutingContext ctx, boolean fromUrl) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of(fromUrl ? "url" : "title", String.class));
      String title = body.getString("title");
      if (fromUrl) title = title.replace("_", " ");

      PageData data = pageProvider.getByTitle(title);
      if (data == null) throw new BadDataException("Invalid page title: " + title);
      ctx.response().setStatusCode(200);
      ctx.json(Utils.pageToJson(data));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void createPage(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(
          body, Map.of("title", String.class, "category", Integer.class, "user", Integer.class));
      String title = body.getString("title");
      Integer category = body.getInteger("category");
      Integer user = body.getInteger("user");

      int id = pageProvider.createPage(new PageData(null, user, title, category, null, null));
      ctx.response().setStatusCode(201);
      ctx.json(new JsonObject().put("id", id));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void updateTitle(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("title", String.class, "id", Integer.class));
      String title = body.getString("title");
      Integer id = body.getInteger("id");

      pageProvider.updatePageTitle(id, title);
      ctx.response().setStatusCode(204).end();
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void updateCategory(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("category", Integer.class, "id", Integer.class));
      Integer category = body.getInteger("category");
      Integer id = body.getInteger("id");

      pageProvider.updatePageCategory(id, category);
      ctx.response().setStatusCode(204).end();
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void toggleArchive(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("value", Boolean.class, "id", Integer.class));
      Boolean val = body.getBoolean("value");
      Integer id = body.getInteger("id");

      pageProvider.toggleArchive(id, val);
      ctx.response().setStatusCode(204).end();
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
