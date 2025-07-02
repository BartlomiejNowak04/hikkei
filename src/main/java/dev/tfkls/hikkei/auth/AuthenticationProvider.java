package dev.tfkls.hikkei.auth;

import io.mikael.urlbuilder.UrlBuilder;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.net.URL;

abstract class AuthenticationProvider {
  protected AuthenticationProvider(URL authRoute) {
    this.authRoute = authRoute;
  }

  interface Callback {
    void onAuthenticate(String authToken, HttpServerResponse response);
  }

  protected final URL authRoute;
  protected Callback authCallback = null;

  /// The user-facing name of this authentication provider.
  /// (displayed by the login screen & frontend)
  public abstract String getName();

  /// Handles the request within the /{authType} route.
  public abstract void handleRequest(RoutingContext context);

  /// Get the path to redirect to when attempting to authenticate with this provider.
  /// Takes a path to redirect to after a successful authentication (callback-oriented).
  public URL getRedirect(URL redirectAfterAuth) {
    return getRedirectBuilder(redirectAfterAuth).toUrl();
  }

  protected UrlBuilder getRedirectBuilder(URL redirectAfterAuth) {
    return UrlBuilder.fromUrl(authRoute).addParameter("redirect_uri", redirectAfterAuth.toString());
  }

  /// Set the callback to be called after a user is properly authenticated.
  /// The callback takes an HTTP response which should've been set to redirect
  /// to the redirectAfterAuth path.
  public void setCallback(Callback callback) {
    authCallback = callback;
  }
}
