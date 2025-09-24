package dev.tfkls.hikkei.model.permission;

import io.vertx.ext.web.RoutingContext;

public interface PermissionVerifier {
  void verify(RoutingContext ctx, PermissionLevel requiredLevel);
}
