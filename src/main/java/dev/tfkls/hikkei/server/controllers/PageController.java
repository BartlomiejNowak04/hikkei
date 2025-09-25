package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
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

public class PageController {
  private final PageDataProvider pageProvider;
  private final AccessManager accessManager;

  public PageController(PageDataProvider pageProvider, AccessManager accessManager) {
    this.pageProvider = pageProvider;
    this.accessManager = accessManager;
  }

  // NOTE: We treat page metadata as public, as adding permission filtering would warrant a major
  // refactor – instead the page metadata is not guarded by permissions, we guard the content
  // instead.

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
        Map.of("title", Type.Str, "category", Type.Int),
        null,
        body -> {
          String title = body.getString("title");
          Integer category = body.getInteger("category");
          Integer user = accessManager.requireUser(ctx);

          accessManager.setCategory(ctx, category);
          accessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);

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
          checkOwnerOrAdmin(ctx, body.getInteger("id"));

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
          checkOwnerOrAdmin(ctx, body.getInteger("id"));

          accessManager.setCategory(ctx, body.getInteger("category"));
          accessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);

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
          checkOwnerOrAdmin(ctx, body.getInteger("id"));
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
          checkOwnerOrAdmin(ctx, body.getInteger("id"));
          pageProvider.deletePage(body.getInteger("id"));
          ctx.response().setStatusCode(204).end();
        });
  }

  // We can usually act on a page IF we are an owner of the page or an admin.
  private void checkOwnerOrAdmin(RoutingContext ctx, int pageId) throws InternalErrorException {
    PageData pageData = pageProvider.getById(pageId);

    OptionalInt maybeUserId = accessManager.fetchUser(ctx);
    if (maybeUserId.isEmpty() || maybeUserId.getAsInt() != pageData.userId()) {

      accessManager.setCategory(ctx, pageData.category());
      accessManager.verifyPermission(ctx, PermissionLevel.ADMIN);
    }
  }
}
