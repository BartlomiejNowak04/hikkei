package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.model.user.AuthData;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class AuthController {
  private final Map<String, AuthenticationProvider> auth;
  private final AuthEventHandler authHandler;

  public AuthController(AuthEventHandler authHandler) {
    this.auth = new HashMap<>();
    this.authHandler = authHandler;
  }

  public void registerProvider(String name, AuthenticationProvider provider) {
    auth.put(name, provider);
  }

  public void getProviderList(RoutingContext ctx) {
    JsonArray array = new JsonArray();
    auth.forEach((name, provider) -> array.add(name));

    ctx.response()
        .setStatusCode(200)
        .putHeader("content-type", "application/json")
        .end(array.toBuffer());
  }

  public void handleLogin(String providerName, RoutingContext ctx) {
    handleRequest(providerName, ctx, authHandler::onLogin);
  }

  public void handleRegister(String providerName, RoutingContext ctx) {
    handleRequest(providerName, ctx, authHandler::onRegister);
  }

  public void handleMerge(String providerName, RoutingContext ctx) {
    handleRequest(providerName, ctx, authHandler::onMerge);
  }

  protected interface ExtendedAuthenticationCallback {
    void callback(AuthData authData, RoutingContext ctx) throws AuthenticationException;
  }

  protected void handleRequest(
      String providerName, RoutingContext ctx, ExtendedAuthenticationCallback callback) {
    URI redirectUri =
        auth.get(providerName)
            .requestAuthentication(
                (token, new_ctx) -> callback.callback(new AuthData(providerName, token), new_ctx));
    ctx.redirect(redirectUri.toString());
  }
}
