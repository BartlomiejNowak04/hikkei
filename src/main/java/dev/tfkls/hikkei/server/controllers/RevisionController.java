package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.util.ParamFlow.handle;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.StoredVersion;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.page.PageRenderer;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.provider.page.IdentityPageRenderer;
import dev.tfkls.hikkei.server.util.AccessManager;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.Type;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

public class RevisionController {
  private final RevisionDataProvider revisionProvider;
  private final VersionProvider versionProvider;
  private final PageDataProvider pageProvider;
  private final PageRenderer pageRenderer;

  public RevisionController(
      RevisionDataProvider revisionProvider,
      VersionProvider versionProvider,
      PageDataProvider pageProvider,
      PageRenderer pageRenderer) {
    this.revisionProvider = revisionProvider;
    this.versionProvider = versionProvider;
    this.pageProvider = pageProvider;
    this.pageRenderer = pageRenderer;
  }

  public void addRevision(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("page_id", Type.Int, "content", Type.Str),
        null,
        body -> {
          int userId = AccessManager.requireUser(ctx);

          int pageCategory = pageProvider.getById(body.getInteger("page_id")).category();
          AccessManager.setCategory(ctx, pageCategory);
          AccessManager.verifyPermission(ctx, PermissionLevel.TRUSTED);

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
                  new RevisionData(body.getInteger("page_id"), userId, filename));
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
          RevisionData data = revisionProvider.getById(body.getInteger("id"));
          if (data != null) {
            int pageCategory = pageProvider.getById(data.pageId()).category();
            AccessManager.setCategory(ctx, pageCategory);
            AccessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);

            revisionProvider.changeState(body.getInteger("id"), body.getBoolean("status"));
            ctx.response().setStatusCode(204).end();
          } else {
            ctx.response().setStatusCode(404).end();
          }
        });
  }

  private void getList(RoutingContext ctx, String type) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Integer id = body.getInteger("id");

          List<RevisionData> data;
          if (type.equals("page")) {
            int pageCategory = pageProvider.getById(id).category();
            AccessManager.setCategory(ctx, pageCategory);
            AccessManager.verifyPermission(ctx, PermissionLevel.UNTRUSTED);

            data = revisionProvider.getByPage(id);
          } else if (type.equals("user")) {
            OptionalInt userId = AccessManager.fetchUser(ctx);
            // Only global moderators can see other users' contributions
            if (userId.isEmpty() || userId.getAsInt() != id) {
              AccessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);
            }

            data = revisionProvider.getByUser(id);
          } else {
            throw new IllegalArgumentException("Expected 'page' or 'user' as the type");
          }

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

  public void getCurrentContent(RoutingContext ctx) {
    getCurrentContent(ctx, pageRenderer);
  }

  public void getCurrentContentRaw(RoutingContext ctx) {
    getCurrentContent(ctx, new IdentityPageRenderer());
  }

  protected void getCurrentContent(RoutingContext ctx, PageRenderer transform) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Integer id = body.getInteger("id");

          PageData pageData = pageProvider.getById(id);
          if (pageData == null) throw new BadDataException("Invalid page id");
          AccessManager.setCategory(ctx, pageData.category());
          AccessManager.verifyPermission(ctx, PermissionLevel.UNTRUSTED);

          List<RevisionData> revisions = revisionProvider.getByPage(id);
          if (revisions == null) throw new BadDataException("Invalid page id");
          revisions.removeIf(rev -> rev.approved() == null || !rev.approved());

          String data = "";
          if (!revisions.isEmpty()) {
            StoredVersion file = versionProvider.getVersion(revisions.getFirst().file());
            if (file == null || file.data() == null) {
              throw new InternalErrorException("Couldn't retrieve content for page " + id);
            }
            data = file.data();
          }
          ctx.response().setStatusCode(200);

          ctx.json(new JsonObject().put("content", transform.render(data)));
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
            int pageCategory = pageProvider.getById(data.pageId()).category();
            AccessManager.setCategory(ctx, pageCategory);
            AccessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);

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
          int userId = AccessManager.requireUser(ctx);
          int pageId = body.getInteger("page_id");

          int pageCategory = pageProvider.getById(pageId).category();
          AccessManager.setCategory(ctx, pageCategory);
          AccessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);

          byte[] id = revisionProvider.getDraft(userId, pageId);
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
          int userId = AccessManager.requireUser(ctx);
          int pageId = body.getInteger("page");
          int pageCategory = pageProvider.getById(pageId).category();
          AccessManager.setCategory(ctx, pageCategory);
          AccessManager.verifyPermission(ctx, PermissionLevel.TRUSTED);

          byte[] id = revisionProvider.getDraft(userId, pageId);
          byte[] nid;
          try {
            nid = versionProvider.storeVersion(body.getString("content"), id);
          } catch (BadDataException e) {
            throw new InternalErrorException(e.getMessage());
          }
          revisionProvider.saveDraft(userId, pageId, nid);
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteDraft(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("page", Type.Int),
        null,
        body -> {
          int userId = AccessManager.requireUser(ctx);
          int pageId = body.getInteger("page");
          int pageCategory = pageProvider.getById(pageId).category();
          AccessManager.setCategory(ctx, pageCategory);
          AccessManager.verifyPermission(ctx, PermissionLevel.TRUSTED);

          byte[] id = revisionProvider.getDraft(userId, pageId);
          revisionProvider.deleteDraft(userId, pageId);
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
          RevisionData revisionPage = revisionProvider.getById(body.getInteger("id"));
          if (revisionPage != null) {
            int pageCategory = pageProvider.getById(revisionPage.pageId()).category();
            AccessManager.setCategory(ctx, pageCategory);
            AccessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);

            revisionProvider.deleteRevision(body.getInteger("id"));
          }
          ctx.response().setStatusCode(204).end();
        });
  }

  public void getRevisionContentRaw(RoutingContext ctx) {
    getRevisionContent(ctx, new IdentityPageRenderer());
  }

  public void getRevisionContent(RoutingContext ctx) {
    getRevisionContent(ctx, pageRenderer);
  }

  protected void getRevisionContent(RoutingContext ctx, PageRenderer transform) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Integer id = body.getInteger("id");
          RevisionData revision = revisionProvider.getById(id);
          if (revision == null) throw new HttpException(404, "Revision not found");

          AccessManager.setCategory(ctx, pageProvider.getById(revision.pageId()).category());
          AccessManager.verifyPermission(ctx, PermissionLevel.TRUSTED);

          StoredVersion file = versionProvider.getVersion(revision.file());
          if (file == null || file.data() == null) {
            throw new InternalErrorException("Couldn't retrieve content for page " + id);
          }
          String data = file.data();
          ctx.response().setStatusCode(200);

          ctx.json(new JsonObject().put("content", transform.render(data)));
        });
  }

  public void render(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("content", Type.Str),
        null,
        body -> {
          String content = body.getString("content");
          ctx.response().setStatusCode(200);
          ctx.json(JsonObject.of("content", pageRenderer.render(content)));
        });
  }
}
