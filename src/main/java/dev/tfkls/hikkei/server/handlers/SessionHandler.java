package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.auth.SessionProvider;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import java.util.OptionalInt;
import java.util.UUID;

public class SessionHandler implements Handler<RoutingContext> {
  public SessionHandler(SessionProvider provider) {
    this(provider, "HikkeiSession", "UserIdentifier");
  }

  public SessionHandler(SessionProvider provider, String cookieName, String contextName) {
    this.provider = provider;
    this.cookieName = cookieName;
    this.contextName = contextName;
  }

  private SessionProvider provider;
  private String cookieName;
  private String contextName;

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
    OptionalInt userId = provider.getUserBySession(sessionId);
    if (userId.isPresent()) {
      ctx.put(contextName, userId.getAsInt());
    } else {
      expireCookie(ctx);
    }
    ctx.next();
  }

  private void expireCookie(RoutingContext ctx) {
    ctx.response().addCookie(Cookie.cookie(cookieName, "").setMaxAge(0));
  }
}
