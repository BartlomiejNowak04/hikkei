package dev.tfkls.hikkei.server.routing;

import static dev.tfkls.hikkei.server.util.Jsonifier.pageToJson;
import static dev.tfkls.hikkei.server.util.Jsonifier.revisionToJson;
import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageManager;
import dev.tfkls.hikkei.model.page.RevisionData;
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
  private final PageManager pageManager;

  public PageRouteHandler(PageManager manager) {
    pageManager = manager;
  }

  private void getRandomPage(RoutingContext ctx) {
    PageData data = pageManager.getRandomPage();
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
    List<PageData> data = pageManager.searchByTitle(title);
    JsonArray items = new JsonArray();
    for (PageData item : data) items.add(pageToJson(item));

    response.setStatusCode(200);
    response.end(items.encode());
  }

  private void getRevisions(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    List<RevisionData> data = pageManager.getRevisions(id);
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
    String data = pageManager.getCurrentContent(id);
    if (data == null) {
      throwError(400, "No content found for page " + id, response);
      return;
    }

    response.setStatusCode(200);
    response.end(new JsonObject().put("content", data).encode());
  }

  private void addRevision(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    String content = body.getString("content");
    if (pageManager.createRevision(id, content)) {
      response.setStatusCode(201);
      response.end();
    } else throwError(500, "Failed to create revision", response);
  }

  @Override
  public void addRoutes(Router router, String url) {
    // common handlers
    router.route(url + "/*").handler(BodyHandler.create());
    router.route(url + "/*").handler(new JsonHeaderHandler());

    router.get(url + "/random").handler(this::getRandomPage);

    router.post(url + "/search").handler(new ArgumentCheckHandler(Map.of("title", String.class)));
    router.post(url + "/search").handler(this::searchByTitle);

    router.post(url + "/revisions").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post(url + "/revisions").handler(this::getRevisions);

    router.post(url + "/current").handler(new ArgumentCheckHandler(Map.of("id", Integer.class)));
    router.post(url + "/current").handler(this::getCurrentContent);

    router
        .post(url + "/update")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put(url + "/update").handler(this::addRevision);
  }
}
