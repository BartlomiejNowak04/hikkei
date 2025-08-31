package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.checkBody;
import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.version.VersionProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;

public class PageController {
  private final PageDataProvider pageProvider;
  private final VersionProvider versionProvider;

  public PageController(PageDataProvider pageProvider, VersionProvider versionProvider) {
    this.pageProvider = pageProvider;
    this.versionProvider = versionProvider;
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

      List<PageData> data = pageProvider.searchPageByTitle(title);
      JsonArray items = new JsonArray();
      for (PageData item : data) items.add(Utils.pageToJson(item));
      ctx.response().setStatusCode(200);
      ctx.json(items);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getRevisions(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class));
      Integer id = body.getInteger("id");

      List<RevisionData> data = pageProvider.getPageRevisions(id);
      if (data == null) throw new BadDataException("Incorrect parameter value: id");
      JsonArray items = new JsonArray();
      for (RevisionData item : data) items.add(Utils.revisionToJson(item));
      ctx.response().setStatusCode(200);
      ctx.json(items);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getCurrentContent(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class));
      Integer id = body.getInteger("id");

      List<RevisionData> revisions = pageProvider.getPageRevisions(id);
      revisions.removeIf(rev -> rev.approved() == null || !rev.approved());

      String data = "";
      if (!revisions.isEmpty()) {
        data = versionProvider.getContent(revisions.getFirst().filename());
        if (data == null)
          throw new InternalErrorException("Couldn't retrieve content for page " + id);
      }
      ctx.response().setStatusCode(200);
      // TODO: transform the data appropriately
      ctx.json(new JsonObject().put("content", data));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getComments(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class));
      Integer id = body.getInteger("id");

      List<CommentData> data = pageProvider.getPageComments(id);
      if (data == null) throw new BadDataException("Incorrect parameter value: id");
      JsonArray items = new JsonArray();
      for (CommentData item : data) items.add(Utils.commentToJson(item));

      ctx.response().setStatusCode(200);
      ctx.json(items);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getPageByTitle(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("key", String.class, "url", Boolean.class));
      String key = body.getString("key");
      if (body.getBoolean("url")) key = key.replace("_", " ");

      PageData data = pageProvider.getPageByTitle(key);
      if (data == null) throw new BadDataException("Invalid page title: " + key);
      ctx.response().setStatusCode(200);
      ctx.json(Utils.pageToJson(data));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void createPage(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("title", String.class, "category", Integer.class));
      String title = body.getString("title");
      Integer category = body.getInteger("category");

      int id = pageProvider.createPage(new PageData(null, title, category, null));
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
