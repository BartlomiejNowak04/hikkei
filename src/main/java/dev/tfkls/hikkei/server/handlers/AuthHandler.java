package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class AuthHandler implements Handler<RoutingContext> {
  private final UserDataProvider userDataProvider;
  private final CategoryDataProvider categoryDataProvider;
  private final String userContextName;
  private final String thisContextName;

  public AuthHandler(UserDataProvider userDataProvider, CategoryDataProvider categoryDataProvider) {
    this(userDataProvider, categoryDataProvider, "AuthenticatedUser", "AuthHandler");
  }

  public AuthHandler(
      UserDataProvider userDataProvider,
      CategoryDataProvider categoryDataProvider,
      String userContextName,
      String thisContextName) {
    this.userDataProvider = userDataProvider;
    this.categoryDataProvider = categoryDataProvider;
    this.userContextName = userContextName;
    this.thisContextName = thisContextName;
  }

  @Override
  public void handle(RoutingContext ctx) {
    ctx.put(thisContextName, this);
  }

  public void verify(
      RoutingContext ctx, PermissionLevel targetLevel, @Nullable Integer categoryId) {
    Object userIdOrNull = ctx.get(userContextName);
    PermissionLevel userLevel = PermissionLevel.NO_ACCESS;
    PermissionLevel categoryLevel = PermissionLevel.NO_ACCESS;

    if (categoryId != null) {
      try {
        categoryLevel = categoryDataProvider.getById((int) categoryId).defaultPermission();
      } catch (InternalErrorException err) {
        ctx.fail(500, err);
      }
    }

    if (userIdOrNull != null) {
      try {
        userLevel = userDataProvider.getUserData((int) userIdOrNull).minimalPermission();
      } catch (InternalErrorException err) {
        ctx.fail(500, err);
      }

      if (categoryId != null) {
        try {
          Optional<PermissionLevel> jointPermission =
              categoryDataProvider.getUserPermission(categoryId, (int) userIdOrNull);
          if (jointPermission.isPresent()) {
            categoryLevel = jointPermission.get();
          }
        } catch (InternalErrorException | BadDataException err) {
          ctx.fail(500, err);
        }
      }
    }

    PermissionLevel trueLevel = PermissionLevel.max(userLevel, categoryLevel);

    if (PermissionLevel.isLess(trueLevel, targetLevel)) {
      if (userIdOrNull == null) {
        ctx.fail(401);
      } else {
        ctx.fail(403);
      }
    }
  }
}
