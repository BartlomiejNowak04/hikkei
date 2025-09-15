package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthenticationCallback;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.util.BoundedMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2AuthorizationURL;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import io.vertx.ext.web.RoutingContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class VertxOAuth2AuthenticationProvider implements AuthenticationProvider {
  public static int DEFAULT_CONCURRENT_REQUESTS_LIMIT = 1000;

  public VertxOAuth2AuthenticationProvider(
      Vertx vertx,
      OAuth2Auth oauthAuth,
      URI redirectUri,
      Function<JsonObject, String> infoAccessor) {
    this(vertx, oauthAuth, redirectUri, infoAccessor, DEFAULT_CONCURRENT_REQUESTS_LIMIT);
  }

  public VertxOAuth2AuthenticationProvider(
      Vertx vertx,
      OAuth2Auth oauthAuth,
      URI redirectUri,
      Function<JsonObject, String> infoAccessor,
      int concurrentRequestsLimit) {
    this.vertx = vertx;
    this.oauthAuth = oauthAuth;
    this.authRequests = new BoundedMap<>(concurrentRequestsLimit);
    this.infoAccessor = infoAccessor;
    this.callbackUri = redirectUri;
  }

  private final Vertx vertx;
  private final OAuth2Auth oauthAuth;

  private URI callbackUri;
  private final Function<JsonObject, String> infoAccessor;

  private final BoundedMap<UUID, AuthenticationCallback> authRequests;

  @Override
  public void handle(RoutingContext ctx) {
    UUID state;
    AuthenticationCallback callback;
    try {
      state = UUID.fromString(ctx.request().getParam("state"));
      callback = authRequests.remove(state).orElseThrow();
    } catch (IllegalArgumentException | NullPointerException | NoSuchElementException ex) {
      ctx.fail(400);
      return;
    }
    String code;
    try {
      code = Objects.requireNonNull(ctx.request().getParam("code"));
    } catch (NullPointerException ex) {
      ctx.fail(400);
      return;
    }

    try {
      User userObj =
          oauthAuth
              .authenticate(
                  new Oauth2Credentials().setCode(code).setRedirectUri(callbackUri.toString()))
              .await();

      callback.onAuthenticate(infoAccessor.apply(oauthAuth.userInfo(userObj).await()), ctx);
    } catch (Exception ex) {
      ctx.fail(400);
    }
  }

  @Override
  public URI requestAuthentication(AuthenticationCallback callback) {
    UUID stateId = UUID.randomUUID();
    authRequests.put(stateId, callback);

    try {
      return new URI(
          oauthAuth.authorizeURL(
              new OAuth2AuthorizationURL()
                  .setRedirectUri(callbackUri.toString())
                  .setScopes(List.of())
                  .setState(stateId.toString())));
    } catch (URISyntaxException ex) {
      throw new IllegalStateException("OAuth2Auth.authorizeURL returned an invalid URI.", ex);
    }
  }
}
