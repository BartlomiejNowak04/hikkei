package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.checkBody;
import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class UserController {
  private final UserDataProvider userProvider;
  private final FileProvider fileProvider;

  public UserController(UserDataProvider provider, FileProvider fileProvider) {
    this.userProvider = provider;
    this.fileProvider = fileProvider;
  }

  public void updatePfp(RoutingContext ctx) {
    try {
      String user = ctx.request().getFormAttribute("id");
      if (user == null) throw new BadDataException("Missing required parameter: id");
      int id = Integer.parseInt(user);
      if (!userProvider.doesUserExist(id)) throw new BadDataException("Incorrect parameter: id");

      for (FileUpload fileUpload : ctx.fileUploads()) {
        String mime = fileUpload.contentType();
        if (!mime.equals("image/jpeg") && !mime.equals("image/png"))
          throw new BadDataException("Unsupported image type");
        byte[] data = Files.readAllBytes(Paths.get(fileUpload.uploadedFileName()));
        byte[] tag = fileProvider.storeContent(data, mime);
        userProvider.updateUserPicture(id, tag);
      }
      ctx.response().setStatusCode(200).end();
    } catch (NumberFormatException e) {
      BadDataException ex = new BadDataException("Incorrect parameter: id");
      ctx.fail(exceptionToCode(ex), ex);
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }

  public void getPfp(RoutingContext ctx) {
    try {
      JsonObject body = ctx.body().asJsonObject();
      checkBody(body, Map.of("id", Integer.class));
      Integer id = body.getInteger("id");

      UserData user = userProvider.getUserData(id);
      if (user == null) throw new BadDataException("Incorrect parameter: id");
      if (user.picture() == null) ctx.response().setStatusCode(204).end();
      else {
        StoredFile file = fileProvider.getContent(user.picture());
        ctx.response()
            .putHeader("Content-Type", file.mime())
            .setStatusCode(200)
            .end(Buffer.buffer(file.data()));
      }
    } catch (Exception e) {
      ctx.fail(exceptionToCode(e), e);
    }
  }
}
