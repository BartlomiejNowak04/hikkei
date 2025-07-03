package dev.tfkls.hikkei.auth;

import io.mikael.urlbuilder.UrlBuilder;
import io.vertx.core.Vertx;
import io.vertx.ext.web.client.WebClient;
import java.net.URL;
import java.security.SecureRandom;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;

public class OAuth2ProviderFactory {
  Vertx vertx;
  Supplier<RandomGenerator> secureRandom;

  public OAuth2ProviderFactory(Vertx vertx) {
    this(vertx, SecureRandom::new);
  }

  public OAuth2ProviderFactory(Vertx vertx, Supplier<RandomGenerator> secureRandom) {
    this.vertx = vertx;
    this.secureRandom = secureRandom;
  }

  public OAuth2Provider githubAuth(URL authRoute, String clientId, String clientSecret) {
    OAuth2Provider.Specification spec =
        new OAuth2Provider.Specification(
            "GitHub",
            UrlBuilder.fromString("https://github.com/login/oauth/authorize").toUrl(),
            UrlBuilder.fromString("https://github.com/login/oauth/access_token").toUrl(),
            clientId,
            clientSecret,
            "",
            true,
            (token, type, client) -> {
              if (!"bearer".equalsIgnoreCase(type)) {
                throw new IllegalStateException("Unsupported OAuth token type: " + type);
              }
              var userJson =
                  client
                      .get("https://api.github.com/user")
                      .bearerTokenAuthentication(token)
                      .send()
                      .await()
                      .bodyAsJsonObject();
              return userJson.getInteger("id").toString();
            });
    return new OAuth2Provider(spec, authRoute, secureRandom.get(), WebClient.create(vertx));
  }
}
