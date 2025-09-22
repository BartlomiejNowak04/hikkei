package dev.tfkls.hikkei.model.auth;

import dev.tfkls.hikkei.model.user.AuthData;
import io.vertx.ext.web.RoutingContext;

public interface AuthEventHandler {
  void onLogin(AuthData authData, RoutingContext ctx);

  void onRegister(AuthData authData, RoutingContext ctx);

  void onMerge(AuthData authData, RoutingContext ctx);
}
