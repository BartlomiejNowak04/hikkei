package dev.tfkls.hikkei.provider.permission;

import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.permission.PermissionVerifier;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;

public class DummyPermissionVerifier implements PermissionVerifier {
  private final PermissionLevel globalRequiredPermission;

  public DummyPermissionVerifier() {
    this(PermissionLevel.NO_ACCESS);
  }

  public DummyPermissionVerifier(PermissionLevel globalRequiredPermission) {
    this.globalRequiredPermission = globalRequiredPermission;
  }

  @Override
  public void verify(RoutingContext ctx, PermissionLevel requiredLevel) {
    if (PermissionLevel.isLess(requiredLevel, globalRequiredPermission)) {
      throw new HttpException(403);
    }
  }
}
