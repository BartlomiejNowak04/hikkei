package dev.tfkls.hikkei.model.auth;

import io.vertx.ext.web.RoutingContext;

public interface AuthenticationCallback {
  void onAuthenticate(String token, RoutingContext responseContext);
}
