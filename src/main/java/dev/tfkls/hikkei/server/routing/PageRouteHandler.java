package dev.tfkls.hikkei.server.routing;

import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageManager;
import dev.tfkls.hikkei.model.page.RevisionData;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.List;

public class PageRouteHandler implements RouteHandler {
  private final PageManager pageManager;

  public PageRouteHandler(PageManager manager) {
    pageManager = manager;
  }

  private JsonObject pageToJson(PageData page) {
    return new JsonObject()
        .put("id", page.id())
        .put("title", page.title())
        .put("category", page.category())
        .put("archived", page.archived());
  }

  private JsonObject revisionToJson(RevisionData revision) {
    return new JsonObject()
        .put("page_id", revision.pageId())
        .put("filename", revision.filename().toString())
        .put("date", revision.date().toString())
        .put("approved", revision.approved());
  }

  private void getRandomPage(RoutingContext ctx) {
    PageData data = pageManager.getRandomPage();
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");

    if (data == null) {
      response.setStatusCode(404);
      response.end();
    } else {
      response.setStatusCode(200);
      response.end(pageToJson(data).encode());
    }
  }

  private void searchByTitle(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    JsonObject body = ctx.body().asJsonObject();

    if (!body.containsKey("title")) {
      response.setStatusCode(400);
      response.end(new JsonObject().put("message", "Missing required parameter: title").encode());
    } else {
      String title = body.getString("title");
      List<PageData> data = pageManager.searchByTitle(title);
      JsonArray items = new JsonArray();
      for (PageData item : data) items.add(pageToJson(item));

      response.setStatusCode(200);
      response.end(items.encode());
    }
  }

  private void getRevisions(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    response.putHeader("content-type", "application/json");
    JsonObject body = ctx.body().asJsonObject();

    if (!body.containsKey("id")) {
      response.setStatusCode(400);
      response.end(new JsonObject().put("message", "Missing required parameter: id").encode());
      return;
    }

    Integer id = body.getInteger("id");
    if (id == null) {
      response.setStatusCode(400);
      response.end(new JsonObject().put("message", "Incorrect parameter: id").encode());
      return;
    }
    List<RevisionData> data = pageManager.getRevisions(id);
    if (data == null) {
      response.setStatusCode(400);
      response.end(new JsonObject().put("message", "Incorrect parameter value: id").encode());
      return;
    }
    JsonArray items = new JsonArray();
    for (RevisionData item : data) items.add(revisionToJson(item));

    response.setStatusCode(200);
    response.end(items.encode());
  }

  @Override
  public void addRoutes(Router router, String url) {
    router.get(url + "/random").handler(this::getRandomPage);
    router.route(url + "/search").handler(BodyHandler.create());
    router.post(url + "/search").handler(this::searchByTitle);
    router.route(url + "/revisions").handler(BodyHandler.create());
    router.post(url + "/revisions").handler(this::getRevisions);
  }
}
