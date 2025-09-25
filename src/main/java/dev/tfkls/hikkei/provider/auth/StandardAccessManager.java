package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.permission.PermissionVerifier;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.Objects;
import java.util.OptionalInt;
import org.jetbrains.annotations.Nullable;

public class StandardAccessManager implements AccessManager {
  private final String permissionVerifierContext;
  private final String categoryContext;
  private final String userContext;

  public StandardAccessManager() {
    this("PermissionVerifier", "CategoryIdentifier", "UserIdentifier");
  }

  public StandardAccessManager(
      String permissionVerifierContext, String categoryContext, String userContext) {
    this.permissionVerifierContext = permissionVerifierContext;
    this.categoryContext = categoryContext;
    this.userContext = userContext;
  }

  @Override
  public void setCategory(RoutingContext ctx, int categoryId) {
    ctx.put(categoryContext, categoryId);
  }

  @Override
  public void verifyPermission(RoutingContext ctx, PermissionLevel level) {
    ((PermissionVerifier) ctx.get(permissionVerifierContext)).verify(ctx, level);
  }

  @Override
  public int requireUser(RoutingContext ctx) {
    try {
      return Objects.requireNonNull(ctx.get(userContext));
    } catch (NullPointerException ex) {
      throw new HttpException(401, "You need to be logged in to perform this operation.");
    }
  }

  @Override
  public OptionalInt fetchUser(RoutingContext ctx) {
    @Nullable Integer userId = ctx.get(userContext);
    if (userId == null) {
      return OptionalInt.empty();
    } else {
      return OptionalInt.of(userId);
    }
  }
}
