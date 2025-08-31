package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;
import static dev.tfkls.hikkei.server.utils.ResponseCloser.endWithStatus;
import static dev.tfkls.hikkei.server.utils.ResponseCloser.throwError;

import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;

public class RevisionController {
  private final RevisionDataProvider revisionProvider;
  private final VersionProvider versionProvider;

  public RevisionController(
      RevisionDataProvider revisionProvider, VersionProvider versionProvider) {
    this.revisionProvider = revisionProvider;
    this.versionProvider = versionProvider;
  }

  public void addRevision(RoutingContext ctx) {
    HttpServerResponse response = ctx.response();
    JsonObject body = ctx.body().asJsonObject();

    Integer id = body.getInteger("id");
    String content = body.getString("content");
    try {
      UUID filename = versionProvider.storeContent(content);
      revisionProvider.createRevision(new RevisionData(null, id, filename, null, null));
      endWithStatus(201, response);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void changeStatus(RoutingContext ctx) {
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
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
