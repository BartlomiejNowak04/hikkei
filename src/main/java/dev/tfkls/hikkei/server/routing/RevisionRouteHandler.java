package dev.tfkls.hikkei.server.routing;

import static dev.tfkls.hikkei.server.util.ResponseCloser.endWithStatus;
import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.revision.RevisionManager;
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
    if (revisionManager.createRevision(id, content)) endWithStatus(201, response);
    else throwError(500, "Failed to create revision", response);
  }

  // TODO: add middleware to authorize
  private void changeStatus(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    Integer status = body.getInteger("status");
    if (status < 0 || status > 2) {
      throwError(400, "Invalid revision status", response);
      return;
    }

    // 0,1,2 -- rejected,pending,approved
    Boolean st;
    if (status == 0) st = false;
    else if (status == 1) st = null;
    else st = true;

    if (revisionManager.changeStatus(id, st)) endWithStatus(200, response);
    else throwError(400, "Invalid revision id " + id, response);
  }

  @Override
  public void addRoutes(Router router, String url) {
    router.route(url + "/*").handler(BodyHandler.create());
    router.route(url + "/*").handler(new JsonHeaderHandler());

    router
        .put(url + "/create")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put(url + "/create").handler(this::addRevision);

    router
        .post(url + "/status")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "status", Integer.class)));
    router.post(url + "/status").handler(this::changeStatus);
  }
}
