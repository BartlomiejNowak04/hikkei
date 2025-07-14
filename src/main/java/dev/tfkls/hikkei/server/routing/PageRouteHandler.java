package dev.tfkls.hikkei.server.routing;

import static dev.tfkls.hikkei.server.util.Jsonifier.*;
import static dev.tfkls.hikkei.server.util.ResponseCloser.endWithStatus;
import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.page.CommentData;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.server.routing.handler.ArgumentCheckHandler;
import dev.tfkls.hikkei.server.routing.handler.JsonHeaderHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.List;
import java.util.Map;

public class PageRouteHandler implements RouteHandler {
  private final PageDataProvider pageProvider;
  private final VersionProvider versionProvider;

  public PageRouteHandler(PageDataProvider pageProvider, VersionProvider versionProvider) {
    this.pageProvider = pageProvider;
    this.versionProvider = versionProvider;
  }

  private void getRandomPage(RoutingContext ctx) {
    PageData data = pageProvider.getRandomPage();
    HttpServerResponse response = ctx.response();

    if (data == null) throwError(404, "No pages available", response);
    else {
      response.setStatusCode(200);
      response.end(pageToJson(data).encode());
    }
  }

  private void searchByTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String title = body.getString("title");
    List<PageData> data = pageProvider.searchPageByTitle(title);
    JsonArray items = new JsonArray();
    for (PageData item : data) items.add(pageToJson(item));

    response.setStatusCode(200);
    response.end(items.encode());
  }

  private void getRevisions(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    List<RevisionData> data = pageProvider.getPageRevisions(id);
    if (data == null) {
      throwError(400, "Incorrect parameter value: id", response);
      return;
    }
    JsonArray items = new JsonArray();
    for (RevisionData item : data) items.add(revisionToJson(item));

    response.setStatusCode(200);
    response.end(items.encode());
  }

  private void getCurrentContent(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    List<RevisionData> revisions = pageProvider.getPageRevisions(id);
    revisions.removeIf(rev -> rev.approved() == null || !rev.approved());
    if (revisions.isEmpty()) {
      throwError(404, "No content found for page " + id, response);
      return;
    }

    String data = versionProvider.getContent(revisions.getFirst().filename());
    if (data == null) {
      throwError(404, "Could not retrieve content for page " + id, response);
      return;
    }

    response.setStatusCode(200);
    response.end(new JsonObject().put("content", data).encode());
  }

  private void getComments(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    List<CommentData> data = pageProvider.getPageComments(id);
    if (data == null) {
      throwError(400, "Incorrect parameter value: id", response);
      return;
    }
    JsonArray items = new JsonArray();
    for (CommentData item : data) items.add(commentToJson(item));

    response.setStatusCode(200);
    response.end(items.encode());
  }

  private void addComment(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    String content = body.getString("content");
    if (pageProvider.addComment(new CommentData(null, id, content, null, null)) != null)
      endWithStatus(201, response);
    else throwError(500, "Unable to add comment", response);
  }

  private void deleteComment(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    if (pageProvider.deleteComment(id)) endWithStatus(200, response);
    else throwError(500, "Unable to delete comment " + id, response);
  }

  private void getPageByTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String key = body.getString("key");
    if (body.getBoolean("url")) key = key.replace("_", " ");

    PageData data = pageProvider.getPageByTitle(key);
    if (data == null) throwError(404, "Invalid page title: " + key, response);
    else {
      response.setStatusCode(200);
      response.end(pageToJson(data).encode());
    }
  }

  @Override
  public void addRoutes(Router router, String url) {
    router.route(url + "/*").handler(BodyHandler.create());
    router.route(url + "/*").handler(new JsonHeaderHandler());

    router.get(url + "/random").handler(this::getRandomPage);

    router.post(url + "/search").handler(new ArgumentCheckHandler(Map.of("title", String.class)));
    router.post(url + "/search").handler(this::searchByTitle);

    router.post(url + "/revisions").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post(url + "/revisions").handler(this::getRevisions);

    router.post(url + "/current").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post(url + "/current").handler(this::getCurrentContent);

    router.post(url + "/comments").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post(url + "/comments").handler(this::getComments);

    router
        .put(url + "/comment/add")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put(url + "/comment/add").handler(this::addComment);

    router
        .post(url + "/comment/delete")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post(url + "/comment/delete").handler(this::deleteComment);

    router
        .post(url + "/get")
        .handler(new ArgumentCheckHandler(Map.of("key", String.class, "url", Boolean.class)));
    router.post(url + "/get").handler(this::getPageByTitle);
  }
}
