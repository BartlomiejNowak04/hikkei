package dev.tfkls.hikkei.permission;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PermissionVerifier;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.provider.permission.StandardPermissionVerifier;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;
import org.junit.jupiter.api.Test;

public class StandardPermissionVerifierMockTest {
  @Test
  public void noUserNoCategorySuccess() {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(null);
    when(ctx.get("CategoryIdentifier")).thenReturn(null);

    var userP = mock(UserDataProvider.class);
    var categoryP = mock(CategoryDataProvider.class);

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.NO_ACCESS);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void noUserNoCategoryFailure() {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(null);
    when(ctx.get("CategoryIdentifier")).thenReturn(null);

    var userP = mock(UserDataProvider.class);
    var categoryP = mock(CategoryDataProvider.class);

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.UNTRUSTED);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(ctx).fail(401);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserNoCategorySuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(null);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.TRUSTED));

    var categoryP = mock(CategoryDataProvider.class);

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.TRUSTED);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(userP).getUserData(1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserNoCategoryFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(null);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.TRUSTED));

    var categoryP = mock(CategoryDataProvider.class);

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.SUPER_ADMIN);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(ctx).fail(403);
    verify(userP).getUserData(1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void noUserYesCategorySuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(null);
    when(ctx.get("CategoryIdentifier")).thenReturn(1);

    var userP = mock(UserDataProvider.class);

    var categoryP = mock(CategoryDataProvider.class);
    when(categoryP.getById(1))
        .thenReturn(new CategoryData(1, "example", null, PermissionLevel.UNTRUSTED));

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.NO_ACCESS);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(categoryP).getById(1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void noUserYesCategoryFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(null);
    when(ctx.get("CategoryIdentifier")).thenReturn(1);

    var userP = mock(UserDataProvider.class);

    var categoryP = mock(CategoryDataProvider.class);
    when(categoryP.getById(1))
        .thenReturn(new CategoryData(1, "example", null, PermissionLevel.UNTRUSTED));

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.TRUSTED);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(ctx).fail(401);
    verify(categoryP).getById(1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserYesCategoryNoOverrideSuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(2);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.NO_ACCESS));

    var categoryP = mock(CategoryDataProvider.class);
    when(categoryP.getById(2))
        .thenReturn(new CategoryData(2, "example", null, PermissionLevel.TRUSTED));
    when(categoryP.getUserPermission(2, 1)).thenReturn(Optional.empty());

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.TRUSTED);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(userP).getUserData(1);
    verify(categoryP).getById(2);
    verify(categoryP).getUserPermission(2, 1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserYesCategoryNoOverrideFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(2);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.NO_ACCESS));

    var categoryP = mock(CategoryDataProvider.class);
    when(categoryP.getById(2))
        .thenReturn(new CategoryData(2, "example", null, PermissionLevel.UNTRUSTED));
    when(categoryP.getUserPermission(2, 1)).thenReturn(Optional.empty());
    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.TRUSTED);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(ctx).fail(403);
    verify(userP).getUserData(1);
    verify(categoryP).getById(2);
    verify(categoryP).getUserPermission(2, 1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserYesCategoryYesOverrideSuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(2);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.UNTRUSTED));

    var categoryP = mock(CategoryDataProvider.class);
    when(categoryP.getById(2))
        .thenReturn(new CategoryData(2, "example", null, PermissionLevel.TRUSTED));
    when(categoryP.getUserPermission(2, 1)).thenReturn(Optional.of(PermissionLevel.ADMIN));

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.TRUSTED);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(userP).getUserData(1);
    verify(categoryP).getById(2);
    verify(categoryP).getUserPermission(2, 1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserYesCategoryYesOverrideFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(2);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.UNTRUSTED));

    var categoryP = mock(CategoryDataProvider.class);
    when(categoryP.getById(2))
        .thenReturn(new CategoryData(2, "example", null, PermissionLevel.SUPER_ADMIN));
    when(categoryP.getUserPermission(2, 1)).thenReturn(Optional.of(PermissionLevel.ADMIN));

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.SUPER_ADMIN);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(ctx).fail(403);
    verify(userP).getUserData(1);
    verify(categoryP).getById(2);
    verify(categoryP).getUserPermission(2, 1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserYesCategoryYesOverrideSuccessTwo() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(2);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.ADMIN));

    var categoryP = mock(CategoryDataProvider.class);
    when(categoryP.getById(2))
        .thenReturn(new CategoryData(2, "example", null, PermissionLevel.UNTRUSTED));
    when(categoryP.getUserPermission(2, 1)).thenReturn(Optional.of(PermissionLevel.TRUSTED));

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    permissionVerifier.verify(ctx, PermissionLevel.ADMIN);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(userP).getUserData(1);
    verify(categoryP).getById(2);
    verify(categoryP).getUserPermission(2, 1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void yesUserYesCategoryYesOverrideFailureTwo() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(2);

    var userP = mock(UserDataProvider.class);
    when(userP.getUserData(1))
        .thenReturn(new UserData(1, "example", null, null, PermissionLevel.ADMIN));

    var categoryP = mock(CategoryDataProvider.class);

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);
    when(categoryP.getById(2))
        .thenReturn(new CategoryData(2, "example", null, PermissionLevel.UNTRUSTED));
    when(categoryP.getUserPermission(2, 1)).thenReturn(Optional.of(PermissionLevel.TRUSTED));

    permissionVerifier.verify(ctx, PermissionLevel.SUPER_ADMIN);

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(ctx).fail(403);
    verify(userP).getUserData(1);
    verify(categoryP).getById(2);
    verify(categoryP).getUserPermission(2, 1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }

  @Test
  public void userInternalErrorFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    when(ctx.get("UserIdentifier")).thenReturn(1);
    when(ctx.get("CategoryIdentifier")).thenReturn(null);

    var userP = mock(UserDataProvider.class);
    InternalErrorException exc = new InternalErrorException("");
    when(userP.getUserData(1)).thenThrow(exc);

    var runtimeMarker = new RuntimeException("expected error");
    doThrow(runtimeMarker).when(ctx).fail(500, exc);
    var categoryP = mock(CategoryDataProvider.class);

    PermissionVerifier permissionVerifier = new StandardPermissionVerifier(userP, categoryP);

    assertThrows(
        RuntimeException.class, () -> permissionVerifier.verify(ctx, PermissionLevel.UNTRUSTED));

    verify(ctx).get("UserIdentifier");
    verify(ctx).get("CategoryIdentifier");
    verify(ctx).fail(500, exc);
    verify(userP).getUserData(1);
    verifyNoMoreInteractions(ctx, userP, categoryP);
  }
}
