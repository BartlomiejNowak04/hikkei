package dev.tfkls.hikkei.server.routing;

import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageManager;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class PageRouteHandler implements RouteHandler {
  private final PageManager pageManager;

  public PageRouteHandler(PageManager manager) {
    pageManager = manager;
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
      JsonObject json = new JsonObject();
      json.put("id", data.id());
      json.put("title", data.title());
      json.put("category", data.category());
      json.put("archived", data.archived());
      response.end(json.encode());
    }
  }

  @Override
  public void addRoutes(Router router, String url) {
    router.get(url + "/random").handler(this::getRandomPage);
  }
}
