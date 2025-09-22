package dev.tfkls.hikkei.server.util;

import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.model.page.PermissionVerifier;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import io.vertx.ext.web.RoutingContext;
import java.util.Objects;

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
      ctx.fail(401, new AuthenticationException("You need to be logged in to view this resource."));
      throw new RuntimeException("Reached an unreachable code path");
    }
  }
}
