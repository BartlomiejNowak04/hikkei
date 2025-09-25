package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AccessManager;
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
  private final AccessManager accessManager;

  public StandardAuthEventHandler(
      UserDataProvider userDataProvider,
      SessionProvider sessionProvider,
      AccessManager accessManager) {
    this(userDataProvider, sessionProvider, accessManager, "HikkeiSession");
  }

  StandardAuthEventHandler(
      UserDataProvider userDataProvider,
      SessionProvider sessionProvider,
      AccessManager accessManager,
      String cookieName) {
    this.userDataProvider = userDataProvider;
    this.sessionProvider = sessionProvider;
    this.accessManager = accessManager;
    this.cookieName = cookieName;
  }

  @Override
  public void onLogin(AuthData authData, RoutingContext ctx) {
    try {
      UserData userData = userDataProvider.getUserByAuth(authData);
      UUID session = sessionProvider.createSession(userData.id());
      ctx.response()
          .addCookie(
              Cookie.cookie(cookieName, session.toString())
                  .setMaxAge(60 * 60 * 24 * 365)
                  .setPath("/api/"));
      ctx.redirect("/auth/success");
    } catch (InternalErrorException err) {
      throw new HttpException(500, err);
    } catch (NullPointerException err) {
      throw new HttpException(
          400, new AuthenticationException("Could not login user with provided data."));
    }
  }

  @Override
  public void onRegister(AuthData authData, RoutingContext ctx) {
    checkIfAuthIsUnused(authData, ctx);

    int newUser = -1;
    try {
      // They can change the name later on, this warrants that we won't fail by giving
      // an already used username (within a reasonable time frame, of course :)
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
        // This only happens in very rare race conditions when multiple users register at once.
        userDataProvider.deleteUser(newUser);
      } catch (InternalErrorException ignored) {
      }
      throw new HttpException(500, err);
    }
  }

  @Override
  public void onMerge(AuthData authData, RoutingContext ctx) {
    int userId = accessManager.requireUser(ctx);

    if (checkIfAuthIsUnused(authData, ctx, userId)) {
      ctx.redirect("/auth/success");
      return;
    }

    try {
      userDataProvider.addUserAuthMethod(userId, authData);
      ctx.redirect("/auth/success");
    } catch (InternalErrorException | BadDataException err) {
      throw new HttpException(500, err);
    } catch (NullPointerException err) {
      throw new HttpException(404, err);
    }
  }

  private void checkIfAuthIsUnused(AuthData authData, RoutingContext ctx) {
    checkIfAuthIsUnused(authData, ctx, null);
  }

  private boolean checkIfAuthIsUnused(AuthData authData, RoutingContext ctx, Integer allowedId) {
    try {
      UserData oldUser = userDataProvider.getUserByAuth(authData);
      if (oldUser != null) {
        if (allowedId != null && allowedId.intValue() == oldUser.id()) {
          return true;
        } else {
          throw new HttpException(
              400,
              new AuthenticationException("User account with said credentials already exists."));
        }
      }
    } catch (InternalErrorException err) {
      throw new HttpException(500, err);
    }
    return false;
  }
}
