package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
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
        Map.of("id", Integer.class, "content", String.class, "user", Integer.class),
        body -> {
          byte[] previous = null;
          List<RevisionData> revs = revisionProvider.getByPage(body.getInteger("id"));
          if (revs != null && !revs.isEmpty()) previous = revs.getFirst().file();
          byte[] filename = versionProvider.storeVersion(body.getString("content"), previous);

          int idr =
              revisionProvider.createRevision(
                  new RevisionData(body.getInteger("id"), body.getInteger("user"), filename));
          ctx.response().setStatusCode(201);
          ctx.json(new JsonObject().put("id", idr));
        });
  }

  public void changeStatus(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class, "status", Boolean.class),
        body -> {
          revisionProvider.changeState(body.getInteger("id"), body.getBoolean("status"));
          ctx.response().setStatusCode(204).end();
        });
  }

  private void getList(RoutingContext ctx, String type) {
    handle(
        ctx,
        Map.of(type, Integer.class),
        body -> {
          Integer id = body.getInteger(type);

          List<RevisionData> data =
              type.equals("page") ? revisionProvider.getByPage(id) : revisionProvider.getByUser(id);
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          JsonArray items = new JsonArray();
          for (RevisionData item : data) items.add(revisionToJson(item));

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
        Map.of("id", Integer.class),
        body -> {
          Integer id = body.getInteger("id");

          List<RevisionData> revisions = revisionProvider.getByPage(id);
          revisions.removeIf(rev -> rev.approved() == null || !rev.approved());

          String data = "";
          if (!revisions.isEmpty()) {
            data = versionProvider.getVersion(revisions.getFirst().file());
            if (data == null)
              throw new InternalErrorException("Couldn't retrieve content for page " + id);
          }
          ctx.response().setStatusCode(200);
          // TODO: transform the data appropriately
          ctx.json(new JsonObject().put("content", data));
        });
  }

  public void getById(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class),
        body -> {
          RevisionData data = revisionProvider.getById(body.getInteger("id"));
          if (data == null) throw new BadDataException("Incorrect parameter value: id");
          else if (data == RevisionData.DELETED) ctx.response().setStatusCode(404).end();
          else {
            ctx.response().setStatusCode(200);
            ctx.json(revisionToJson(data));
          }
        });
  }

  public void getDraft(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("user", Integer.class, "page", Integer.class),
        body -> {
          byte[] id = revisionProvider.getDraft(body.getInteger("user"), body.getInteger("page"));
          if (id == null) throw new BadDataException("Incorrect draft id");
          String content = versionProvider.getVersion(id);
          if (content == null) throw new BadDataException("Incorrect draft id");
          ctx.response().setStatusCode(200);
          ctx.json(new JsonObject().put("content", content));
        });
  }

  public void saveDraft(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("user", Integer.class, "page", Integer.class, "content", String.class),
        body -> {
          byte[] id = revisionProvider.getDraft(body.getInteger("user"), body.getInteger("page"));
          byte[] nid = versionProvider.storeVersion(body.getString("content"), id);
          revisionProvider.saveDraft(body.getInteger("user"), body.getInteger("page"), nid);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteDraft(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("user", Integer.class, "page", Integer.class),
        body -> {
          byte[] id =
              revisionProvider.deleteDraft(body.getInteger("user"), body.getInteger("page"));
          versionProvider.deleteVersion(id);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteRevision(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Integer.class),
        body -> {
          revisionProvider.deleteRevision(body.getInteger("id"));
          ctx.response().setStatusCode(204).end();
        });
  }
}
