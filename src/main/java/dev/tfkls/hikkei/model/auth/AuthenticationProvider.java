package dev.tfkls.hikkei.model.auth;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.net.URI;

/// An authentication provider.
public interface AuthenticationProvider extends Handler<RoutingContext> {
  /// Request an authentication flow.
  /// @param callback An callback to run when a user is succesfully authenticated
  /// @return A redirect URI with which to start the authentication flow.
  URI requestAuthentication(AuthenticationCallback callback);
}
