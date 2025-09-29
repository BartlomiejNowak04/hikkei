package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.util.ParamFlow.handle;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.Type;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

public class UserController {
  private final UserDataProvider userProvider;
  private final FileProvider fileProvider;
  private final CategoryDataProvider categoryProvider;
  private final AccessManager accessManager;

  public UserController(
      UserDataProvider provider,
      FileProvider fileProvider,
      CategoryDataProvider categoryProvider,
      AccessManager accessManager) {
    this.userProvider = provider;
    this.fileProvider = fileProvider;
    this.categoryProvider = categoryProvider;
    this.accessManager = accessManager;
  }

  public void getSelf(RoutingContext ctx) {
    int userId = accessManager.requireUser(ctx);
    ctx.pathParams().put("id", String.valueOf(userId));
    getUser(ctx);
  }

  public void getUser(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          UserData data = userProvider.getUserData(body.getInteger("id"));
          if (data == null) {
            ctx.response().setStatusCode(400);
            ctx.json(new JsonObject().put("message", "Incorrect parameter: id"));
          } else if (data == UserData.DELETED) {
            ctx.response().setStatusCode(404);
            ctx.json(new JsonObject().put("message", "This user is deleted"));
          } else {
            ctx.response().setStatusCode(200);
            ctx.json(Jsonify.userToJson(data));
          }
        });
  }

  public void getUserByName(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("name", Type.Str),
        body -> {
          UserData data = userProvider.getUserByName(body.getString("name"));
          if (data == null) {
            ctx.response().setStatusCode(400);
            ctx.json(new JsonObject().put("message", "Incorrect parameter: id"));
          } else if (data == UserData.DELETED) {
            ctx.response().setStatusCode(404);
            ctx.json(new JsonObject().put("message", "This user is deleted"));
          } else {
            ctx.response().setStatusCode(200);
            ctx.json(Jsonify.userToJson(data));
          }
        });
  }

  public void updatePfp(RoutingContext ctx) {
    try {
      int id = accessManager.requireUser(ctx);
      if (!userProvider.doesUserExist(id)) throw new BadDataException("Incorrect parameter: id");
      if (ctx.fileUploads().size() != 1)
        throw new BadDataException("Exactly one file must be supplied");

      for (FileUpload fileUpload : ctx.fileUploads()) {
        String mime = fileUpload.contentType();
        if (!mime.equals("image/jpeg") && !mime.equals("image/png"))
          throw new BadDataException("Unsupported image type");
        byte[] data = Files.readAllBytes(Paths.get(fileUpload.uploadedFileName()));
        byte[] tag = fileProvider.storeContent(data, mime);
        userProvider.updateUserPicture(id, tag);
      }
      ctx.response().setStatusCode(204).end();
    } catch (Exception e) {
      ctx.fail(e);
    }
  }

  public void getPfp(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          UserData user = userProvider.getUserData(body.getInteger("id"));
          if (user == null || user == UserData.DELETED)
            throw new BadDataException("Incorrect parameter: id");
          if (user.picture() == null) ctx.response().setStatusCode(204).end();
          else {
            StoredFile file = fileProvider.getContent(user.picture());
            if (file == null) throw new InternalErrorException("File not found");
            ctx.response()
                .putHeader("Content-Type", file.mime())
                .setStatusCode(200)
                .end(Buffer.buffer(file.data()));
          }
        });
  }

  public void getCategoriesWithPermissions(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        body -> {
          Map<Integer, PermissionLevel> vals =
              userProvider.getCategoriesPermissions(body.getInteger("id"));
          if (vals == null) throw new BadDataException("Incorrect parameter: id");
          JsonArray items = new JsonArray();
          for (Map.Entry<Integer, PermissionLevel> val : vals.entrySet()) {
            CategoryData data = categoryProvider.getById(val.getKey());
            if (data == null || data == CategoryData.DELETED) continue;
            items.add(Jsonify.categoryToJson(data).put("permission", val.getValue().toString()));
          }
          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void createUser(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("name", Type.Str),
        null,
        body -> {
          accessManager.verifyPermission(ctx, PermissionLevel.ADMIN);
          int id = userProvider.createUser(new UserData(body.getString("name")));
          ctx.response().setStatusCode(201);
          ctx.json(new JsonObject().put("id", id));
        });
  }

  // TODO: TEST THIS
  public void updatePermission(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Type.Int, "permission", Type.Str),
        null,
        body -> {
          PermissionLevel newLevel;
          try {
            newLevel = PermissionLevel.valueOf(body.getString("permission"));
          } catch (IllegalArgumentException e) {
            throw new HttpException(400, "Invalid permission enum value.");
          }
          UserData userData;
          try {
            userData = Objects.requireNonNull(userProvider.getUserData(body.getInteger("id")));
          } catch (NullPointerException e) {
            throw new HttpException(404, "User not found.");
          }
          PermissionLevel oldLevel = userData.globalPermission();
          PermissionLevel maxLevel = PermissionLevel.max(newLevel, oldLevel);

          accessManager.verifyPermission(ctx, PermissionLevel.successor(maxLevel));

          userProvider.updateGlobalPermission(userData.id(), newLevel);
          ctx.response().setStatusCode(200).end();
        });
  }

  public void updateName(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("name", Type.Str),
        null,
        body -> {
          int id = accessManager.requireUser(ctx);
          userProvider.updateUsername(id, body.getString("name"));
          ctx.response().setStatusCode(204).end();
        });
  }

  public void deleteUser(RoutingContext ctx) {
    handle(
        ctx,
        null,
        null,
        body -> {
          int id = accessManager.requireUser(ctx);
          userProvider.deleteUser(id);
          ctx.response().setStatusCode(204).end();
        });
  }
}
