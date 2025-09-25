package dev.tfkls.hikkei.model.auth;

import dev.tfkls.hikkei.model.permission.PermissionLevel;
import io.vertx.ext.web.RoutingContext;
import java.util.OptionalInt;

public interface AccessManager {
  void setCategory(RoutingContext ctx, int categoryId);

  void verifyPermission(RoutingContext ctx, PermissionLevel level);

  int requireUser(RoutingContext ctx);

  OptionalInt fetchUser(RoutingContext ctx);
}
