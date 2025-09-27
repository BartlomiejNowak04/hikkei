package dev.tfkls.hikkei.model.auth;

import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import io.vertx.ext.web.RoutingContext;

public interface AuthenticationCallback {
  void onAuthenticate(String token, RoutingContext responseContext) throws AuthenticationException;
}
