package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.permission.PermissionVerifier;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class StandardAccessManager implements AccessManager {
  private final PermissionVerifier permissionVerifier;
  private final String categoryContext;
  private final String userContext;

  public StandardAccessManager(PermissionVerifier permissionVerifier) {
    this(permissionVerifier, "CategoryIdentifier", "UserIdentifier");
  }

  public StandardAccessManager(
      PermissionVerifier permissionVerifier, String categoryContext, String userContext) {
    this.permissionVerifier = permissionVerifier;
    this.categoryContext = categoryContext;
    this.userContext = userContext;
  }

  @Override
  public void setCategory(RoutingContext ctx, int categoryId) {
    ctx.put(categoryContext, categoryId);
  }

  @Override
  public void verifyPermission(RoutingContext ctx, PermissionLevel level) {
    permissionVerifier.verify(ctx, level);
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
  public Optional<Integer> fetchUser(RoutingContext ctx) {
    @Nullable Integer userId = ctx.get(userContext);
    if (userId == null) {
      return Optional.empty();
    } else {
      return Optional.of(userId);
    }
  }
}
