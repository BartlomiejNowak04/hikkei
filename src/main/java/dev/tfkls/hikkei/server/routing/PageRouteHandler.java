package dev.tfkls.hikkei.server.routing;

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

  private void userError(String message, HttpServerResponse response) {
    response.setStatusCode(400);
    response.end(new JsonObject().put("message", message).encode());
  }

  private void getRandomPage(RoutingContext ctx) {
    PageData data = pageManager.getRandomPage();
    HttpServerResponse response = ctx.response();

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
      userError("Incorrect parameter value: id", response);
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
      userError("No content found for page " + id, response);
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
    } else {
      response.setStatusCode(500);
      response.end();
    }
  }

  @Override
  public void addRoutes(Router router, String url) {
    // common handlers
    router.route(url + "/*").handler(BodyHandler.create());
    router.route(url + "/*").handler(new JsonHeaderHandler());

    router.get(url + "/random").handler(this::getRandomPage);

    router.post(url + "/search").handler(new ArgumentCheckHandler(List.of("title")));
    router.post(url + "/search").handler(this::searchByTitle);

    router.post(url + "/revisions").handler(new ArgumentCheckHandler(List.of("id")));
    router.post(url + "/revisions").handler(this::getRevisions);

    router.post(url + "/current").handler(new ArgumentCheckHandler(List.of("id")));
    router.post(url + "/current").handler(this::getCurrentContent);

    router.post(url + "/update").handler(new ArgumentCheckHandler(List.of("id", "content")));
    router.put(url + "/update").handler(this::addRevision);
  }
}
