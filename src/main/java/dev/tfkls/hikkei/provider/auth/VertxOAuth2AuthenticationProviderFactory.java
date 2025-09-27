package dev.tfkls.hikkei.provider.auth;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.oauth2.providers.GithubAuth;
import io.vertx.ext.auth.oauth2.providers.GoogleAuth;
import java.net.URI;
import java.util.List;

public class VertxOAuth2AuthenticationProviderFactory {
  public static VertxOAuth2AuthenticationProvider github(
      Vertx vertx, URI redirectUri, String clientId, String clientSecret) {
    return new VertxOAuth2AuthenticationProvider(
        vertx,
        GithubAuth.create(vertx, clientId, clientSecret),
        redirectUri,
        json -> json.getLong("id").toString());
  }

  public static VertxOAuth2AuthenticationProvider google(
      Vertx vertx, URI redirectUri, String clientId, String clientSecret) {
    return new VertxOAuth2AuthenticationProvider(
        vertx,
        GoogleAuth.create(vertx, clientId, clientSecret),
        redirectUri,
        json -> json.getString("id"),
        List.of("openid"));
  }
}
