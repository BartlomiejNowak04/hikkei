package dev.tfkls.hikkei.server.routes;

import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.server.controllers.AuthController;
import io.vertx.core.Vertx;
import java.net.URI;
import java.net.URISyntaxException;

public class AuthRouteHandler extends RouteHandler {
  public AuthRouteHandler(Vertx vertx, URI baseUri, AuthEventHandler handler) {
    super(vertx);

    try {
      new URI(baseUri.toString() + "/auth/callback/example");
    } catch (URISyntaxException ex) {
      throw new IllegalArgumentException(
          "Provided baseURI must not have a query or fragment element.", ex);
    }
    this.baseUri = baseUri;

    controller = new AuthController(handler);
    router.get("/auth").blockingHandler(controller::getProviderList);
    router.get("/auth/methods").blockingHandler(controller::getProviderList);
  }

  private final URI baseUri;
  private final AuthController controller;

  AuthRouteHandler registerProvider(String name, AuthenticationProvider provider) {
    controller.registerProvider(name, provider);
    try {
      router
          .route("/auth/callback/" + name)
          .blockingHandler(
              provider.registerProvider(new URI(baseUri.toString() + "/auth/callback/" + name)));
    } catch (URISyntaxException e) {
      throw new IllegalStateException("Provided baseUri has a query or fragment component", e);
    }
    router.route("/auth/login/" + name).blockingHandler(ctx -> controller.handleLogin(name, ctx));
    router
        .route("/auth/register/" + name)
        .blockingHandler(ctx -> controller.handleRegister(name, ctx));
    router.route("/auth/merge/" + name).blockingHandler(ctx -> controller.handleMerge(name, ctx));

    return this;
  }
}
