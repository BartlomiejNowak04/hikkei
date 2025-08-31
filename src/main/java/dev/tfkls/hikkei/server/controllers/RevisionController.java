package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.checkBody;
import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.Map;
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
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class, "content", String.class));
      Integer id = body.getInteger("id");
      String content = body.getString("content");

      UUID filename = versionProvider.storeContent(content);
      int idr = revisionProvider.createRevision(new RevisionData(null, id, filename, null, null));
      ctx.response().setStatusCode(201);
      ctx.json(new JsonObject().put("id", idr));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void changeStatus(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(
          body, Map.of("id", Integer.class, "status", Integer.class, "page_id", Integer.class));
      Integer id = body.getInteger("id");
      Integer pageId = body.getInteger("page_id");
      Integer status = body.getInteger("status");
      if (status < 0 || status > 2) throw new BadDataException("Invalid revision status");

      // 0,1,2 -- rejected,pending,approved
      Boolean st;
      if (status == 0) st = false;
      else if (status == 1) st = null;
      else st = true;
      revisionProvider.changeState(id, pageId, st);
      ctx.response().setStatusCode(204).end();
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
