package dev.tfkls.hikkei.server.util;

import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.server.handlers.AuthHandler;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.Nullable;

public class AuthVerifier {
  public static void verify(RoutingContext ctx, PermissionLevel level) {
    ((AuthHandler) ctx.get("AuthHandler")).verify(ctx, level, null);
  }

  public void verify(RoutingContext ctx, PermissionLevel level, @Nullable Integer categoryId) {
    ((AuthHandler) ctx.get("AuthHandler")).verify(ctx, level, categoryId);
  }
}
