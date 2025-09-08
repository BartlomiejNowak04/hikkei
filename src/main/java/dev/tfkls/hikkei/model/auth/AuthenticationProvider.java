package dev.tfkls.hikkei.model.auth;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.net.URI;

/// An authentication provider.
/// It provides a request handler which handles request on the "{siteUri}/auth/{authName}/..."
// routes.
/// It *MUST* handle the "/login" and "/register" subroutes – this is verified by tests.
/// It *MUST* redirect onto /auth/success after setting a session token
///   or /auth/failure routes after the request loop is done.
public interface AuthenticationProvider {
  /// Register a new authentication provider. This function is called before
  ///   any requests are handled.
  /// @param baseUri URI under which the returned provider will be registered.
  /// @return A request handler, giving the provider a possibility of handling callbacks.
  Handler<RoutingContext> registerProvider(URI baseUri);

  /// Request an authentication flow.
  /// @param callback An callback to run when a user is succesfully authenticated
  /// @return A redirect URI with which to start the authentication flow.
  URI requestAuthentication(AuthenticationCallback callback);
}
