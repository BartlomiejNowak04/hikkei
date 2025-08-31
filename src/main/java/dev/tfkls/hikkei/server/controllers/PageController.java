package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;
import static dev.tfkls.hikkei.server.utils.Jsonifier.*;
import static dev.tfkls.hikkei.server.utils.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.version.VersionProvider;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;

public class PageController {
  private final PageDataProvider pageProvider;
  private final VersionProvider versionProvider;

  public PageController(PageDataProvider pageProvider, VersionProvider versionProvider) {
    this.pageProvider = pageProvider;
    this.versionProvider = versionProvider;
  }

  public void getRandomPage(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    try {
      PageData data = pageProvider.getRandomPage();
      if (data == null) throwError(404, "No pages available", response);
      else {
        response.setStatusCode(200);
        response.end(pageToJson(data).encode());
      }
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void searchByTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String title = body.getString("title");
    try {
      List<PageData> data = pageProvider.searchPageByTitle(title);
      JsonArray items = new JsonArray();
      for (PageData item : data) items.add(pageToJson(item));

      response.setStatusCode(200);
      response.end(items.encode());
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getRevisions(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    try {
      List<RevisionData> data = pageProvider.getPageRevisions(id);
      if (data == null) {
        throwError(400, "Incorrect parameter value: id", response);
        return;
      }
      JsonArray items = new JsonArray();
      for (RevisionData item : data) items.add(revisionToJson(item));

      response.setStatusCode(200);
      response.end(items.encode());
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getCurrentContent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    try {
      List<RevisionData> revisions = pageProvider.getPageRevisions(id);
      revisions.removeIf(rev -> rev.approved() == null || !rev.approved());
      if (revisions.isEmpty()) {
        throwError(404, "No content found for page " + id, response);
        return;
      }

      String data = versionProvider.getContent(revisions.getFirst().filename());
      if (data == null) {
        throwError(404, "Couldn't retrieve content for page " + id, response);
        return;
      }

      response.setStatusCode(200);
      response.end(new JsonObject().put("content", data).encode());
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getComments(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    try {
      List<CommentData> data = pageProvider.getPageComments(id);
      if (data == null) {
        throwError(400, "Incorrect parameter value: id", response);
        return;
      }
      JsonArray items = new JsonArray();
      for (CommentData item : data) items.add(commentToJson(item));

      response.setStatusCode(200);
      response.end(items.encode());
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getPageByTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String key = body.getString("key");
    if (body.getBoolean("url")) key = key.replace("_", " ");

    try {
      PageData data = pageProvider.getPageByTitle(key);
      if (data == null) throwError(404, "Invalid page title: " + key, response);
      else {
        response.setStatusCode(200);
        response.end(pageToJson(data).encode());
      }
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void createPage(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String title = body.getString("title");
    Integer category = body.getInteger("category");
    try {
      pageProvider.createPage(new PageData(null, title, category, null));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void updateTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String title = body.getString("title");
    Integer id = body.getInteger("id");
    try {
      pageProvider.updatePageTitle(id, title);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void updateCategory(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer category = body.getInteger("category");
    Integer id = body.getInteger("id");
    try {
      pageProvider.updatePageCategory(id, category);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void toggleArchive(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Boolean val = body.getBoolean("value");
    Integer id = body.getInteger("id");
    try {
      pageProvider.toggleArchive(id, val);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
