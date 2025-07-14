package dev.tfkls.hikkei.server.routing;

import static dev.tfkls.hikkei.server.util.Jsonifier.*;
import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
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
    HttpServerResponse response = ctx.response();
    try {
      PageData data = pageProvider.getRandomPage();
      if (data == null) throwError(404, "No pages available", response);
      else {
        response.setStatusCode(200);
        response.end(pageToJson(data).encode());
      }
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    }
  }

  private void searchByTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String title = body.getString("title");
    try {
      List<PageData> data = pageProvider.searchPageByTitle(title);
      JsonArray items = new JsonArray();
      for (PageData item : data) items.add(pageToJson(item));

      response.setStatusCode(200);
      response.end(items.encode());
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    }
  }

  private void getRevisions(RoutingContext ctx) {
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
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    }
  }

  private void getCurrentContent(RoutingContext ctx) {
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
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    }
  }

  private void getComments(RoutingContext ctx) {
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
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    }
  }

  private void getPageByTitle(RoutingContext ctx) {
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
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    }
  }

  private void createPage(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String title = body.getString("title");
    Integer category = body.getInteger("category");
    try {
      pageProvider.createPage(new PageData(null, title, category, null));
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    } catch (BadDataException e) {
      throwError(400, e.getMessage(), response);
    }
  }

  private void updateTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    String title = body.getString("title");
    Integer id = body.getInteger("id");
    try {
      pageProvider.updatePageTitle(id, title);
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    } catch (BadDataException e) {
      throwError(400, e.getMessage(), response);
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
        .post(url + "/get")
        .handler(new ArgumentCheckHandler(Map.of("key", String.class, "url", Boolean.class)));
    router.post(url + "/get").handler(this::getPageByTitle);

    router
        .put(url + "/create")
        .handler(
            new ArgumentCheckHandler(Map.of("title", String.class, "category", Integer.class)));
    router.put(url + "/create").handler(this::createPage);

    router
        .post(url + "/update_title")
        .handler(new ArgumentCheckHandler(Map.of("title", String.class, "id", Integer.class)));
    router.post(url + "/update_title").handler(this::updateTitle);
  }
}
