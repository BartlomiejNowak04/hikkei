package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.util.ParamFlow.handle;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.StoredVersion;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.Type;
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
    handle(
        ctx,
        Map.of("page_id", Type.Int, "content", Type.Str),
        null,
        body -> {
          byte[] previous = null;
          List<RevisionData> revs = revisionProvider.getByPage(body.getInteger("page_id"));
          if (revs != null && !revs.isEmpty()) previous = revs.getFirst().file();
          byte[] filename;
          try {
            filename = versionProvider.storeVersion(body.getString("content"), previous);
          } catch (BadDataException e) {
            throw new InternalErrorException(e.getMessage());
          }

          int idr =
              revisionProvider.createRevision(
                  new RevisionData(body.getInteger("page_id"), ctx.get("id"), filename));
          ctx.response().setStatusCode(201);
          ctx.json(new JsonObject().put("id", idr));
        });
  }

  public void changeStatus(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Type.Int, "status", Type.Bool),
        null,
        body -> {
          revisionProvider.changeState(body.getInteger("id"), body.getBoolean("status"));
          ctx.response().setStatusCode(204).end();
        });
  }

  private void getList(RoutingContext ctx, String type) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Integer id = body.getInteger("id");

          List<RevisionData> data =
              type.equals("page") ? revisionProvider.getByPage(id) : revisionProvider.getByUser(id);
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          JsonArray items = new JsonArray();
          for (RevisionData item : data) items.add(Jsonify.revisionToJson(item));

          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
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
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Integer id = body.getInteger("id");

          List<RevisionData> revisions = revisionProvider.getByPage(id);
          if (revisions == null) throw new BadDataException("Invalid page id");
          revisions.removeIf(rev -> rev.approved() == null || !rev.approved());

          String data = "";
          if (!revisions.isEmpty()) {
            StoredVersion file = versionProvider.getVersion(revisions.getFirst().file());
            if (file == null || file.data() == null)
              throw new InternalErrorException("Couldn't retrieve content for page " + id);
            data = file.data();
          }
          ctx.response().setStatusCode(200);
          // TODO: transform the data appropriately
          ctx.json(new JsonObject().put("content", data));
        });
  }

  public void getById(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          RevisionData data = revisionProvider.getById(body.getInteger("id"));
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          else if (data == RevisionData.DELETED) ctx.response().setStatusCode(404).end();
          else {
            ctx.response().setStatusCode(200);
            ctx.json(Jsonify.revisionToJson(data));
          }
        });
  }

  public void getDraft(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("page_id", Type.Int),
        body -> {
          byte[] id = revisionProvider.getDraft(ctx.get("id"), body.getInteger("page_id"));
          String content = "";
          if (id != null) {
            StoredVersion file = versionProvider.getVersion(id);
            if (file == null || file.data() == null)
              throw new InternalErrorException("Incorrect data id");
            content = file.data();
          }
          ctx.response().setStatusCode(200);
          ctx.json(new JsonObject().put("content", content));
        });
  }

  public void saveDraft(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("page", Type.Int, "content", Type.Str),
        null,
        body -> {
          byte[] id = revisionProvider.getDraft(ctx.get("id"), body.getInteger("page"));
          byte[] nid;
          try {
            nid = versionProvider.storeVersion(body.getString("content"), id);
          } catch (BadDataException e) {
            throw new InternalErrorException(e.getMessage());
          }
          revisionProvider.saveDraft(ctx.get("id"), body.getInteger("page"), nid);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteDraft(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("page", Type.Int),
        null,
        body -> {
          byte[] id = revisionProvider.getDraft(ctx.get("id"), body.getInteger("page"));
          revisionProvider.deleteDraft(ctx.get("id"), body.getInteger("page"));
          versionProvider.deleteVersion(id);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteRevision(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Type.Int),
        null,
        body -> {
          revisionProvider.deleteRevision(body.getInteger("id"));
          ctx.response().setStatusCode(204).end();
        });
  }
}
