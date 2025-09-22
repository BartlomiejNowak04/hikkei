package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;

public class StandardAuthEventHandler implements AuthEventHandler {
  private final UserDataProvider userDataProvider;
  private final SessionProvider sessionProvider;
  private final String cookieName;
  private final String contextName;

  public StandardAuthEventHandler(
      UserDataProvider userDataProvider, SessionProvider sessionProvider) {
    this(userDataProvider, sessionProvider, "HikkeiSession", "AuthenticatedUser");
  }

  StandardAuthEventHandler(
      UserDataProvider userDataProvider,
      SessionProvider sessionProvider,
      String cookieName,
      String contextName) {
    this.userDataProvider = userDataProvider;
    this.sessionProvider = sessionProvider;
    this.cookieName = cookieName;
    this.contextName = contextName;
  }

  @Override
  public void onLogin(String providerName, String token, RoutingContext ctx) {
    try {
      UserData userData = userDataProvider.getUserByAuth(new AuthData(providerName, token));
      UUID session = sessionProvider.createSession(userData.id());
      ctx.response()
          .addCookie(Cookie.cookie(cookieName, session.toString()).setMaxAge(60 * 60 * 24 * 365));
      ctx.redirect("/auth/success");
    } catch (InternalErrorException err) {
      ctx.fail(500, err);
    } catch (NullPointerException err) {
      ctx.fail(404, err);
    }
  }

  @Override
  public void onRegister(String providerName, String token, RoutingContext ctx) {
    int newUser = -1;
    try {
      newUser = userDataProvider.createUser(new UserData(UUID.randomUUID().toString()));
    } catch (InternalErrorException | BadDataException err) {
      ctx.fail(500, err);
    } catch (NullPointerException err) {
      ctx.fail(404, err);
    }
    try {
      userDataProvider.addUserAuthMethod(newUser, new AuthData(providerName, token));

      // i love code reuse :)
      onLogin(providerName, token, ctx);
    } catch (InternalErrorException | BadDataException err) {
      try {
        // TODO: drop user here instead
        userDataProvider.deleteUser(newUser);
      } catch (InternalErrorException ignored) {
      }
      ctx.fail(500, err);
    } catch (NullPointerException err) {
      try {
        userDataProvider.deleteUser(newUser);
      } catch (InternalErrorException ignored) {
      }
      ctx.fail(404, err);
    }
  }

  @Override
  public void onMerge(String providerName, String token, RoutingContext ctx) {
    try {
      int userId = ctx.get(contextName);
      userDataProvider.addUserAuthMethod(userId, new AuthData(providerName, token));
    } catch (InternalErrorException | BadDataException err) {
      ctx.fail(500, err);
    } catch (NullPointerException err) {
      ctx.fail(404, err);
    }
  }
}
