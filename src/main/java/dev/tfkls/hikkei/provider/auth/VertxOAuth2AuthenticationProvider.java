package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthenticationCallback;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.util.BoundedMap;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.oauth2.OAuth2Auth;
import io.vertx.ext.auth.oauth2.OAuth2AuthorizationURL;
import io.vertx.ext.auth.oauth2.OAuth2FlowType;
import io.vertx.ext.auth.oauth2.Oauth2Credentials;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;

public class VertxOAuth2AuthenticationProvider implements AuthenticationProvider {
  public static int DEFAULT_CONCURRENT_REQUESTS_LIMIT = 1000;

  public VertxOAuth2AuthenticationProvider(
      Vertx vertx,
      OAuth2Auth oauthAuth,
      URI redirectUri,
      Function<JsonObject, String> infoAccessor) {
    this(vertx, oauthAuth, redirectUri, infoAccessor, List.of());
  }

  public VertxOAuth2AuthenticationProvider(
      Vertx vertx,
      OAuth2Auth oauthAuth,
      URI redirectUri,
      Function<JsonObject, String> infoAccessor,
      Collection<String> oauthScopes) {
    this(
        vertx,
        oauthAuth,
        redirectUri,
        infoAccessor,
        oauthScopes,
        DEFAULT_CONCURRENT_REQUESTS_LIMIT);
  }

  public VertxOAuth2AuthenticationProvider(
      Vertx vertx,
      OAuth2Auth oauthAuth,
      URI redirectUri,
      Function<JsonObject, String> infoAccessor,
      Collection<String> oauthScopes,
      int concurrentRequestsLimit) {
    this.vertx = vertx;
    this.oauthAuth = oauthAuth;
    this.authRequests = new BoundedMap<>(concurrentRequestsLimit);
    this.infoAccessor = infoAccessor;
    this.callbackUri = redirectUri;
    this.oauthScopes = new ArrayList<>(oauthScopes);
  }

  private final Vertx vertx;
  private final OAuth2Auth oauthAuth;
  private final URI callbackUri;
  private final Function<JsonObject, String> infoAccessor;
  private final BoundedMap<UUID, AuthenticationCallback> authRequests;
  private final List<String> oauthScopes;

  @Override
  public void handle(RoutingContext ctx) {
    UUID state;
    AuthenticationCallback callback;
    try {
      state = UUID.fromString(ctx.request().getParam("state"));
      callback = authRequests.remove(state).orElseThrow();
    } catch (IllegalArgumentException | NullPointerException | NoSuchElementException ex) {
      throw new HttpException(
          400, new AuthenticationException("OAuth2 flow error: unable to retrieve OAuth2 state."));
    }
    String code;
    try {
      code = Objects.requireNonNull(ctx.request().getParam("code"));
    } catch (NullPointerException ex) {
      throw new HttpException(
          400, new AuthenticationException("OAuth2 flow error: unable to retrieve OAuth2 code."));
    }

    String userId;
    try {
      User userObj =
          oauthAuth
              .authenticate(
                  new Oauth2Credentials()
                      .setFlow(OAuth2FlowType.AUTH_CODE)
                      .setCode(code)
                      .setRedirectUri(callbackUri.toString()))
              .toCompletionStage()
              .toCompletableFuture()
              .get();

      JsonObject userInfo =
          oauthAuth.userInfo(userObj).toCompletionStage().toCompletableFuture().get();

      userId = infoAccessor.apply(userInfo);
    } catch (Exception ex) {
      throw new HttpException(500, ex);
    }

    callback.onAuthenticate(userId, ctx);
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
                  .setScopes(oauthScopes)
                  .setState(stateId.toString())));
    } catch (URISyntaxException ex) {
      throw new IllegalStateException("OAuth2Auth.authorizeURL returned an invalid URI.", ex);
    }
  }
}
