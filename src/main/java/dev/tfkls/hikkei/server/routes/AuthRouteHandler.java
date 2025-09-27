package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.server.controllers.AuthController;
import dev.tfkls.hikkei.server.handlers.SessionHandler;
import io.vertx.core.Vertx;
import java.net.URI;
import java.net.URISyntaxException;

public class AuthRouteHandler extends RouteHandler {

  public AuthRouteHandler(
      Vertx vertx, URI baseUri, AuthEventHandler handler, SessionHandler sessionHandler) {
    super(vertx);

    try {
      new URI(baseUri.toString() + "/api/auth/callback/example");
    } catch (URISyntaxException ex) {
      throw new IllegalArgumentException(
          "Provided baseURI must not have a query or fragment element.", ex);
    }
    this.baseUri = baseUri;

    controller = new AuthController(handler);
    router.get("/").blockingHandler(controller::getProviderList);
    router.get("/methods").blockingHandler(controller::getProviderList);

    router.get("/logout").blockingHandler(sessionHandler::logout);
    router.get("/logout/global").blockingHandler(sessionHandler::globalLogout);
  }

  private final URI baseUri;
  private final AuthController controller;

  public URI getProviderURI(String providerName) {
    try {
      return new URI(baseUri + "/api/auth/callback/" + providerName);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid providerName was given, can't create an URI.", e);
    }
  }

  public AuthRouteHandler registerProvider(String name, AuthenticationProvider provider) {
    controller.registerProvider(name, provider);
    router.route("/callback/" + name).blockingHandler(provider);
    router.route("/login/" + name).blockingHandler(ctx -> controller.handleLogin(name, ctx));
    router.route("/register/" + name).blockingHandler(ctx -> controller.handleRegister(name, ctx));
    router.route("/merge/" + name).blockingHandler(ctx -> controller.handleMerge(name, ctx));

    return this;
  }
}
