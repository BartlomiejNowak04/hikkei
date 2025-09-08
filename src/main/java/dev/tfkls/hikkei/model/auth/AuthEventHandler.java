package dev.tfkls.hikkei.model.auth;

import io.vertx.ext.web.RoutingContext;

public interface AuthEventHandler {
  void onLogin(String providerName, String token, RoutingContext ctx);

  void onRegister(String providerName, String token, RoutingContext ctx);

  void onMerge(String providerName, String token, RoutingContext ctx);
}
