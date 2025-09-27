package dev.tfkls.hikkei.model.auth;

import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.model.user.AuthData;
import io.vertx.ext.web.RoutingContext;

public interface AuthEventHandler {
  void onLogin(AuthData authData, RoutingContext ctx) throws AuthenticationException;

  void onRegister(AuthData authData, RoutingContext ctx) throws AuthenticationException;

  void onMerge(AuthData authData, RoutingContext ctx) throws AuthenticationException;
}
