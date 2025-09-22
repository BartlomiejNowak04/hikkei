package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import io.vertx.core.http.Cookie;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.util.UUID;

public class StandardAuthEventHandler implements AuthEventHandler {
  private final UserDataProvider userDataProvider;
  private final SessionProvider sessionProvider;
  private final String cookieName;
  private final String contextName;

  public StandardAuthEventHandler(
      UserDataProvider userDataProvider, SessionProvider sessionProvider) {
    this(userDataProvider, sessionProvider, "HikkeiSession", "UserIdentifier");
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
  public void onLogin(AuthData authData, RoutingContext ctx) {
    try {
      UserData userData = userDataProvider.getUserByAuth(authData);
      UUID session = sessionProvider.createSession(userData.id());
      ctx.response()
          .addCookie(Cookie.cookie(cookieName, session.toString()).setMaxAge(60 * 60 * 24 * 365));
      ctx.redirect("/auth/success");
    } catch (InternalErrorException err) {
      throw new HttpException(500, err);
    } catch (NullPointerException err) {
      throw new HttpException(
          400, new AuthenticationException("Auth error: could not login user with provided data."));
    }
  }

  @Override
  public void onRegister(AuthData authData, RoutingContext ctx) {
    checkIfAuthIsUnused(authData, ctx);

    int newUser = -1;
    try {
      newUser = userDataProvider.createUser(new UserData(UUID.randomUUID().toString()));
    } catch (InternalErrorException | BadDataException err) {
      throw new HttpException(500, err);
    }
    try {
      userDataProvider.addUserAuthMethod(newUser, authData);

      // NOTE: I love code reuse :)
      onLogin(authData, ctx);
    } catch (InternalErrorException | BadDataException err) {
      try {
        // NOTE: This may litter the DB with some users, but this should not matter.
        userDataProvider.deleteUser(newUser);
      } catch (InternalErrorException ignored) {
      }
      throw new HttpException(500, err);
    }
  }

  @Override
  public void onMerge(AuthData authData, RoutingContext ctx) {
    checkIfAuthIsUnused(authData, ctx);

    try {
      int userId = ctx.get(contextName);
      userDataProvider.addUserAuthMethod(userId, authData);
      ctx.redirect("/auth/success");
    } catch (InternalErrorException | BadDataException err) {
      throw new HttpException(500, err);
    } catch (NullPointerException err) {
      throw new HttpException(404, err);
    }
  }

  private void checkIfAuthIsUnused(AuthData authData, RoutingContext ctx) {
    try {
      UserData oldUser = userDataProvider.getUserByAuth(authData);
      if (oldUser != null) {
        throw new HttpException(
            400,
            new AuthenticationException(
                "Auth error: user account with said credentials already exists."));
      }
    } catch (InternalErrorException err) {
      throw new HttpException(500, err);
    }
  }
}
