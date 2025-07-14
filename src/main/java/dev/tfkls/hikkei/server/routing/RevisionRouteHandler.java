package dev.tfkls.hikkei.server.routing;

import static dev.tfkls.hikkei.server.util.ResponseCloser.endWithStatus;
import static dev.tfkls.hikkei.server.util.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import dev.tfkls.hikkei.server.routing.handler.ArgumentCheckHandler;
import dev.tfkls.hikkei.server.routing.handler.JsonHeaderHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Map;
import java.util.UUID;

public class RevisionRouteHandler implements RouteHandler {
  private final RevisionDataProvider revisionProvider;
  private final VersionProvider versionProvider;

  public RevisionRouteHandler(
      RevisionDataProvider revisionProvider, VersionProvider versionProvider) {
    this.revisionProvider = revisionProvider;
    this.versionProvider = versionProvider;
  }

  private void addRevision(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    String content = body.getString("content");
    try {
      UUID filename = versionProvider.storeContent(content);
      revisionProvider.createRevision(new RevisionData(null, id, filename, null, null));
      endWithStatus(201, response);
    } catch (BadDataException e) {
      throwError(400, e.getMessage(), response);
    } catch (InternalErrorException e) {
      throwError(500, e.getMessage(), response);
    }
  }

  // TODO: add middleware to authorize
  private void changeStatus(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    Integer pageId = body.getInteger("page_id");
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

    try {
      revisionProvider.changeState(id, pageId, st);
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

    router
        .put(url + "/create")
        .handler(new ArgumentCheckHandler(Map.of("id", Integer.class, "content", String.class)));
    router.put(url + "/create").handler(this::addRevision);

    router
        .post(url + "/status")
        .handler(
            new ArgumentCheckHandler(
                Map.of("id", Integer.class, "status", Integer.class, "page_id", Integer.class)));
    router.post(url + "/status").handler(this::changeStatus);
  }
}
