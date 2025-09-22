package dev.tfkls.hikkei.server.util;

import dev.tfkls.hikkei.model.page.PermissionVerifier;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import io.vertx.ext.web.RoutingContext;

public class PermissionManager {
  public static void setCategory(RoutingContext ctx, int categoryId) {
    ctx.put("CategoryIdentifier", categoryId);
  }

  public static void verify(RoutingContext ctx, PermissionLevel level) {
    ((PermissionVerifier) ctx.get("PermissionVerifier")).verify(ctx, level);
  }
}
