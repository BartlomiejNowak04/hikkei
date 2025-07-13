package dev.tfkls.hikkei.server.routing;

import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.page.RevisionManager;
import dev.tfkls.hikkei.server.routing.handler.ArgumentCheckHandler;
import dev.tfkls.hikkei.server.routing.handler.JsonHeaderHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Map;

public class RevisionRouteHandler implements RouteHandler {
  private final RevisionManager revisionManager;

  public RevisionRouteHandler(RevisionManager revisionManager) {
    this.revisionManager = revisionManager;
  }

  private void addRevision(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    String content = body.getString("content");
    if (revisionManager.createRevision(id, content)) {
      response.setStatusCode(201);
      response.end();
    } else throwError(500, "Failed to create revision", response);
  }

  @Override
  public void addRoutes(Router router, String url) {
    router.route(url + "/*").handler(BodyHandler.create());
    router.route(url + "/*").handler(new JsonHeaderHandler());

    router
        .post(url + "/create")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put(url + "/create").handler(this::addRevision);
  }
}
