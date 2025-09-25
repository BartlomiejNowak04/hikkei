package dev.tfkls.hikkei.server.util;

import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.permission.PermissionVerifier;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.Objects;
import java.util.OptionalInt;
import org.jetbrains.annotations.Nullable;

public class AccessManager {
  public static void setCategory(RoutingContext ctx, int categoryId) {
    ctx.put("CategoryIdentifier", categoryId);
  }

  public static void verifyPermission(RoutingContext ctx, PermissionLevel level) {
    ((PermissionVerifier) ctx.get("PermissionVerifier")).verify(ctx, level);
  }

  public static int requireUser(RoutingContext ctx) {
    try {
      return Objects.requireNonNull(ctx.get("UserIdentifier"));
    } catch (NullPointerException ex) {
      throw new HttpException(401, "You need to be logged in to perform this operation.");
    }
  }

  public static OptionalInt fetchUser(RoutingContext ctx) {
    @Nullable Integer userId = ctx.get("UserIdentifier");
    if (userId == null) {
      return OptionalInt.empty();
    } else {
      return OptionalInt.of(userId);
    }
  }
}
