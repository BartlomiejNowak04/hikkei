package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.util.ParamFlow.*;

import dev.tfkls.hikkei.model.blob.BlobData;
import dev.tfkls.hikkei.model.blob.BlobDataProvider;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.server.util.AccessManager;
import dev.tfkls.hikkei.server.util.Jsonify;
import dev.tfkls.hikkei.server.util.Type;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

public class BlobController {
  private final BlobDataProvider blobProvider;

  public BlobController(BlobDataProvider blobProvider) {
    this.blobProvider = blobProvider;
  }

  public void getUserBlobs(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        json -> {
          int userId = json.getInteger("id");
          List<BlobData> blobs = blobProvider.getUserBlobs(userId);

          JsonArray items = new JsonArray();
          for (BlobData blob : blobs) {
            items.add(Jsonify.blobToJson(blob));
          }

          ctx.response().setStatusCode(200);
          ctx.json(items);
        });
  }

  public void createBlob(RoutingContext ctx) {
    int userId = AccessManager.requireUser(ctx);
    byte[] data = ctx.body().buffer().getBytes();
    String mime =
        Optional.ofNullable(ctx.request().getHeader("Content-Type"))
            .orElse("application/octet-stream");

    try {
      int blobId = blobProvider.createBlob(data, mime, userId);

      ctx.response().setStatusCode(201);
      ctx.json(JsonObject.of("id", blobId));
    } catch (InternalErrorException err) {
      throw new HttpException(500, err);
    }
  }

  public void deleteBlob(RoutingContext ctx) {
    handle(
        ctx,
        Map.of("id", Type.Int),
        null,
        json -> {
          int id = json.getInteger("id");
          BlobData blobData = blobProvider.getBlobById(id);

          if (blobData != null) {
            OptionalInt userId = AccessManager.fetchUser(ctx);
            if (userId.isEmpty() || userId.getAsInt() != blobData.userId()) {
              AccessManager.verifyPermission(ctx, PermissionLevel.MODERATOR);
            }

            blobProvider.deleteBlob(id);
          }

          ctx.response().setStatusCode(204).end();
        });
  }

  public void getBlobData(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        json -> {
          int id = json.getInteger("id");

          BlobData blobData = blobProvider.getBlobById(id);
          byte[] data = blobProvider.getBlobData(id);
          if (data != null && blobData != null) {
            ctx.response().setStatusCode(200);
            ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, blobData.mime());
            ctx.end(Buffer.buffer(data));
          } else {
            ctx.response().setStatusCode(404).end();
          }
        });
  }

  public void getBlobMetadata(RoutingContext ctx) {
    handle(
        ctx,
        null,
        Map.of("id", Type.Int),
        json -> {
          int id = json.getInteger("id");

          BlobData blobData = blobProvider.getBlobById(id);
          if (blobData != null) {
            ctx.response().setStatusCode(200);
            ctx.json(Jsonify.blobToJson(blobData));
          } else {
            ctx.response().setStatusCode(404).end();
          }
        });
  }
}
