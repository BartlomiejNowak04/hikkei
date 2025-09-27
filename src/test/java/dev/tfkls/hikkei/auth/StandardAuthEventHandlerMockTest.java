package dev.tfkls.hikkei.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.model.exceptions.AuthenticationFailureException;
import dev.tfkls.hikkei.model.exceptions.AuthenticationSuccessException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.provider.auth.StandardAuthEventHandler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class StandardAuthEventHandlerMockTest {
  @Test
  public void checkLoginSuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData)).thenReturn(new UserData(1, "bob", null, null, null));
    when(sessP.createSession(1)).thenReturn(sessionId);
    when(ctx.response()).thenReturn(ctxResp);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, mock());

    assertThrows(AuthenticationSuccessException.class, () -> authHandler.onLogin(authData, ctx));

    verify(userP).getUserByAuth(authData);
    verify(sessP).createSession(1);
    verify(ctx).response();
    verify(ctxResp).addCookie(any());

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp);
  }

  @Test
  public void checkLoginFailureNoUser() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData)).thenReturn(null);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, mock());

    assertThrows(AuthenticationFailureException.class, () -> authHandler.onLogin(authData, ctx));

    verify(userP).getUserByAuth(authData);

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp);
  }

  @Test
  public void checkRegisterSuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData))
        .thenReturn(null)
        .thenReturn(new UserData(1, "bob", null, null, null));
    when(userP.createUser(any())).thenReturn(1);

    // onLogin dependencies
    when(ctx.response()).thenReturn(ctxResp);
    when(sessP.createSession(1)).thenReturn(sessionId);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, mock());

    assertThrows(AuthenticationSuccessException.class, () -> authHandler.onRegister(authData, ctx));

    verify(userP, times(2)).getUserByAuth(authData);
    verify(userP).createUser(any());
    verify(userP).addUserAuthMethod(1, authData);

    // On login dependencies
    verify(sessP).createSession(1);
    verify(ctx).response();
    verify(ctxResp).addCookie(any());

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp);
  }

  @Test
  public void checkRegisterFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData)).thenReturn(new UserData(1, "bob", null, null, null));

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, mock());

    assertThrows(AuthenticationFailureException.class, () -> authHandler.onRegister(authData, ctx));

    verify(userP).getUserByAuth(authData);

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp);
  }

  @Test
  public void checkRegisterRaceCondition() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData))
        .thenReturn(null)
        .thenReturn(new UserData(1, "bob", null, null, null));
    when(userP.createUser(any())).thenReturn(1);
    doThrow(new InternalErrorException("Race condition :O"))
        .when(userP)
        .addUserAuthMethod(1, authData);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, mock());

    assertThrows(AuthenticationFailureException.class, () -> authHandler.onRegister(authData, ctx));

    verify(userP).deleteUser(1);
  }

  @Test
  public void checkMergeSuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);
    var access = mock(AccessManager.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData))
        .thenReturn(null)
        .thenReturn(new UserData(1, "bob", null, null, null));
    when(access.requireUser(any())).thenReturn(1);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, access);

    assertThrows(AuthenticationSuccessException.class, () -> authHandler.onMerge(authData, ctx));

    verify(userP).getUserByAuth(authData);
    verify(access).requireUser(any());
    verify(userP).addUserAuthMethod(1, authData);

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp, access);
  }

  @Test
  public void checkMergeFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);
    var access = mock(AccessManager.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData))
        .thenReturn(null)
        .thenReturn(new UserData(1, "bob", null, null, null));
    when(access.requireUser(any())).thenReturn(1);
    doThrow(new InternalErrorException("")).when(userP).addUserAuthMethod(1, authData);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, access);

    assertThrows(AuthenticationFailureException.class, () -> authHandler.onMerge(authData, ctx));

    verify(userP).getUserByAuth(authData);
    verify(access).requireUser(any());
    verify(userP).addUserAuthMethod(1, authData);

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp, access);
  }

  @Test
  public void checkMergeUserExistsFailure() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);
    var access = mock(AccessManager.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData)).thenReturn(new UserData(2, "bob", null, null, null));
    when(access.requireUser(any())).thenReturn(1);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, access);

    assertThrows(AuthenticationFailureException.class, () -> authHandler.onMerge(authData, ctx));

    verify(userP).getUserByAuth(authData);
    verify(access).requireUser(any());

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp, access);
  }

  @Test
  public void checkMergeSameUserSuccess() throws Exception {
    var ctx = mock(RoutingContext.class);
    var userP = mock(UserDataProvider.class);
    var sessP = mock(SessionProvider.class);
    var ctxResp = mock(HttpServerResponse.class);
    var access = mock(AccessManager.class);

    AuthData authData = new AuthData("github", "bob123");
    UUID sessionId = UUID.randomUUID();

    when(userP.getUserByAuth(authData)).thenReturn(new UserData(1, "bob", null, null, null));
    when(access.requireUser(any())).thenReturn(1);

    AuthEventHandler authHandler = new StandardAuthEventHandler(userP, sessP, access);

    assertThrows(AuthenticationSuccessException.class, () -> authHandler.onMerge(authData, ctx));

    verify(userP).getUserByAuth(authData);
    verify(access).requireUser(any());

    verifyNoMoreInteractions(ctx, userP, sessP, ctxResp, access);
  }
}
