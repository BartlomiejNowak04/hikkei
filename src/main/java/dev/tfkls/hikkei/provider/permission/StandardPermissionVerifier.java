package dev.tfkls.hikkei.provider.permission;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PermissionVerifier;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

public class StandardPermissionVerifier implements PermissionVerifier {
  private final UserDataProvider userDataProvider;
  private final CategoryDataProvider categoryDataProvider;
  private final String userContextName;
  private final String categoryContextName;

  public StandardPermissionVerifier(
      UserDataProvider userDataProvider, CategoryDataProvider categoryDataProvider) {
    this(userDataProvider, categoryDataProvider, "UserIdentifier", "CategoryIdentifier");
  }

  public StandardPermissionVerifier(
      UserDataProvider userDataProvider,
      CategoryDataProvider categoryDataProvider,
      String userContextName,
      String categoryContextName) {
    this.userDataProvider = userDataProvider;
    this.categoryDataProvider = categoryDataProvider;
    this.userContextName = userContextName;
    this.categoryContextName = categoryContextName;
  }

  public void verify(RoutingContext ctx, PermissionLevel targetLevel) {
    @Nullable Integer userId = ctx.get(userContextName);
    @Nullable Integer categoryId = ctx.get(categoryContextName);
    PermissionLevel userLevel = PermissionLevel.NO_ACCESS;
    PermissionLevel categoryLevel = PermissionLevel.NO_ACCESS;

    if (categoryId != null) {
      try {
        categoryLevel = categoryDataProvider.getById((int) categoryId).defaultPermission();
      } catch (InternalErrorException err) {
        ctx.fail(500, err);
      }
    }

    if (userId != null) {
      try {
        userLevel = userDataProvider.getUserData((int) userId).minimalPermission();
      } catch (InternalErrorException err) {
        ctx.fail(500, err);
      }

      if (categoryId != null) {
        try {
          Optional<PermissionLevel> jointPermission =
              categoryDataProvider.getUserPermission(categoryId, (int) userId);
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
      if (userId == null) {
        ctx.fail(401);
      } else {
        ctx.fail(403);
      }
    }
  }
}
