package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.page.PermissionVerifier;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class PermissionHandler implements Handler<RoutingContext> {
  private final PermissionVerifier verifier;
  private final String contextName;

  public PermissionHandler(PermissionVerifier verifier) {
    this(verifier, "PermissionVerifier");
  }

  public PermissionHandler(PermissionVerifier verifier, String contextName) {
    this.verifier = verifier;
    this.contextName = contextName;
  }

  @Override
  public void handle(RoutingContext ctx) {
    ctx.put(contextName, verifier);
  }
}
