package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.model.exceptions.AuthenticationSuccessException;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.Nullable;

public class SessionHandler implements Handler<RoutingContext> {

  /// Path the session cookie is issued on by StandardAuthEventHandler. An expiring cookie only
  /// replaces the live one when name, domain and path all match, so it must use the same path.
  public static final String DEFAULT_COOKIE_PATH = "/api/";

  public SessionHandler(SessionProvider provider) {
    this(provider, "HikkeiSession", DEFAULT_COOKIE_PATH, "UserIdentifier", "SessionIdentifier");
  }

  public SessionHandler(
      SessionProvider provider,
      String cookieName,
      String cookiePath,
      String userContextName,
      String sessionContextName) {
    this.provider = provider;
    this.cookieName = cookieName;
    this.cookiePath = cookiePath;
    this.userContextName = userContextName;
    this.sessionContextName = sessionContextName;
  }

  private final SessionProvider provider;
  private final String cookieName;
  private final String cookiePath;
  private final String userContextName;
  private final String sessionContextName;

  @Override
  public void handle(RoutingContext ctx) {
    Cookie sessionCookie = ctx.request().getCookie(cookieName);
    if (sessionCookie == null) {
      ctx.next();
      return;
    }
    UUID sessionId;
    try {
      sessionId = UUID.fromString(sessionCookie.getValue());
    } catch (IllegalArgumentException ex) {
      // Invalid UUID was given in the cookie;
      expireCookie(ctx);
      ctx.next();
      return;
    }
    ctx.put(sessionContextName, sessionId);

    Optional<Integer> userId = provider.getUserBySession(sessionId);
    if (userId.isPresent()) {
      ctx.put(userContextName, userId.get());
    } else {
      expireCookie(ctx);
    }
    ctx.next();
  }

  public void logout(RoutingContext ctx) {
    @Nullable UUID sessionId = ctx.get(sessionContextName);
    if (sessionId != null) {
      provider.dropSession(sessionId);
    }

    expireCookie(ctx);
    throw new AuthenticationSuccessException("Successfully logged out of session");
  }

  public void globalLogout(RoutingContext ctx) {
    @Nullable Integer userId = ctx.get(userContextName);
    if (userId != null) {
      provider.dropUserSessions(userId);
    }

    expireCookie(ctx);
    throw new AuthenticationSuccessException("Successfully logged out of all account sessions");
  }

  private void expireCookie(RoutingContext ctx) {
    ctx.response().addCookie(Cookie.cookie(cookieName, "").setPath(cookiePath).setMaxAge(0));
  }
}
