package dev.tfkls.hikkei.model.page;

import dev.tfkls.hikkei.model.permission.PermissionLevel;
import io.vertx.ext.web.RoutingContext;

public interface PermissionVerifier {
  void verify(RoutingContext ctx, PermissionLevel requiredLevel);
}
