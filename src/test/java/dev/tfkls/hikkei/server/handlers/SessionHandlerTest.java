package dev.tfkls.hikkei.server.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.model.exceptions.AuthenticationSuccessException;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SessionHandlerTest {
  private static final String COOKIE = "HikkeiSession";

  private SessionProvider provider;
  private RoutingContext ctx;
  private HttpServerRequest request;
  private HttpServerResponse response;
  private SessionHandler handler;

  @BeforeEach
  void setUp() {
    provider = mock(SessionProvider.class);
    ctx = mock(RoutingContext.class);
    request = mock(HttpServerRequest.class);
    response = mock(HttpServerResponse.class);
    when(ctx.request()).thenReturn(request);
    when(ctx.response()).thenReturn(response);
    handler = new SessionHandler(provider);
  }

  private Cookie expiredCookie() {
    ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
    verify(response).addCookie(captor.capture());
    return captor.getValue();
  }

  private void assertExpiredOnLoginPath(Cookie cookie) {
    assertEquals(COOKIE, cookie.getName());
    assertEquals("", cookie.getValue());
    assertEquals(0, cookie.getMaxAge());
    assertEquals(SessionHandler.DEFAULT_COOKIE_PATH, cookie.getPath());
  }

  @Test
  void noCookieJustContinues() {
    when(request.getCookie(COOKIE)).thenReturn(null);

    handler.handle(ctx);

    verify(ctx).next();
    verify(ctx, never()).put(any(), any());
    verify(response, never()).addCookie(any());
  }

  @Test
  void malformedCookieIsExpiredOnLoginPath() {
    when(request.getCookie(COOKIE)).thenReturn(Cookie.cookie(COOKIE, "not-a-uuid"));

    handler.handle(ctx);

    assertExpiredOnLoginPath(expiredCookie());
    verify(ctx).next();
    verify(ctx, never()).put(any(), any());
    verifyNoInteractions(provider);
  }

  @Test
  void unknownSessionIsExpiredOnLoginPath() {
    UUID session = UUID.randomUUID();
    when(request.getCookie(COOKIE)).thenReturn(Cookie.cookie(COOKIE, session.toString()));
    when(provider.getUserBySession(session)).thenReturn(Optional.empty());

    handler.handle(ctx);

    assertExpiredOnLoginPath(expiredCookie());
    verify(ctx).put("SessionIdentifier", session);
    verify(ctx, never()).put(eq("UserIdentifier"), any());
    verify(ctx).next();
  }

  @Test
  void validSessionPutsUserInContext() {
    UUID session = UUID.randomUUID();
    when(request.getCookie(COOKIE)).thenReturn(Cookie.cookie(COOKIE, session.toString()));
    when(provider.getUserBySession(session)).thenReturn(Optional.of(7));

    handler.handle(ctx);

    verify(ctx).put("SessionIdentifier", session);
    verify(ctx).put("UserIdentifier", 7);
    verify(response, never()).addCookie(any());
    verify(ctx).next();
  }

  @Test
  void logoutDropsSessionAndExpiresCookieOnLoginPath() {
    UUID session = UUID.randomUUID();
    when(ctx.<UUID>get("SessionIdentifier")).thenReturn(session);

    assertThrows(AuthenticationSuccessException.class, () -> handler.logout(ctx));

    verify(provider).dropSession(session);
    assertExpiredOnLoginPath(expiredCookie());
  }

  @Test
  void globalLogoutDropsUserSessionsAndExpiresCookieOnLoginPath() {
    when(ctx.<Integer>get("UserIdentifier")).thenReturn(7);

    assertThrows(AuthenticationSuccessException.class, () -> handler.globalLogout(ctx));

    verify(provider).dropUserSessions(7);
    assertExpiredOnLoginPath(expiredCookie());
  }

  @Test
  void customCookiePathIsUsedWhenExpiring() {
    handler =
        new SessionHandler(provider, COOKIE, "/custom/", "UserIdentifier", "SessionIdentifier");
    when(request.getCookie(COOKIE)).thenReturn(Cookie.cookie(COOKIE, "garbage"));

    handler.handle(ctx);

    assertEquals("/custom/", expiredCookie().getPath());
  }
}
