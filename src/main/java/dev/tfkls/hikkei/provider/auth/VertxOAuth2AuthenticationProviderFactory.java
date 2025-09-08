package dev.tfkls.hikkei.provider.auth;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.oauth2.providers.GithubAuth;

public class VertxOAuth2AuthenticationProviderFactory {
  public static VertxOAuth2AuthenticationProvider github(
      Vertx vertx, String clientId, String clientSecret) {
    return new VertxOAuth2AuthenticationProvider(
        vertx,
        GithubAuth.create(vertx, clientId, clientSecret),
        json -> json.getLong("id").toString());
  }
}
