package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.version.VersionProvider;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Map;

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
      checkBody(body, Map.of("id", Integer.class, "content", String.class, "user", Integer.class));
      Integer id = body.getInteger("id");
      String content = body.getString("content");
      Integer user = body.getInteger("user");

      byte[] filename = versionProvider.storeContent(content);
      int idr =
          revisionProvider.createRevision(new RevisionData(id, null, user, filename, null, null));
      ctx.response().setStatusCode(201);
      ctx.json(new JsonObject().put("id", idr));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void changeStatus(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class, "status", Boolean.class));
      Integer id = body.getInteger("id");
      Boolean status = body.getBoolean("status");

      revisionProvider.changeState(id, status);
      ctx.response().setStatusCode(204).end();
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  private void getList(RoutingContext ctx, String type) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of(type, Integer.class));
      Integer id = body.getInteger(type);

      List<RevisionData> data =
          type.equals("page") ? revisionProvider.getByPage(id) : revisionProvider.getByUser(id);
      if (data == null) throw new BadDataException("Incorrect parameter value: id");
      JsonArray items = new JsonArray();
      for (RevisionData item : data) items.add(revisionToJson(item));

      ctx.response().setStatusCode(200);
      ctx.json(items);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getByPage(RoutingContext ctx) {
    getList(ctx, "page");
  }

  public void getByUser(RoutingContext ctx) {
    getList(ctx, "user");
  }

  // TODO: do the rendering
  public void Render(RoutingContext ctx) {}

  public void getCurrentContent(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class));
      Integer id = body.getInteger("id");

      List<RevisionData> revisions = revisionProvider.getByPage(id);
      revisions.removeIf(rev -> rev.approved() == null || !rev.approved());

      String data = "";
      if (!revisions.isEmpty()) {
        data = versionProvider.getContent(revisions.getFirst().file());
        if (data == null)
          throw new InternalErrorException("Couldn't retrieve content for page " + id);
      }
      ctx.response().setStatusCode(200);
      // TODO: transform the data appropriately
      ctx.json(new JsonObject().put("content", data));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getById(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class));
      Integer id = body.getInteger("id");

      RevisionData data = revisionProvider.getById(id);
      if (data == null) throw new BadDataException("Incorrect parameter value: id");
      ctx.response().setStatusCode(200);
      ctx.json(revisionToJson(data));
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
