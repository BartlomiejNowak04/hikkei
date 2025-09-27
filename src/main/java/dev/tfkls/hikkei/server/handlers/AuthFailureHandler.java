package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.model.exceptions.AuthenticationSuccessException;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.text.MessageFormat;

public class AuthFailureHandler implements Handler<RoutingContext> {
  private final String path;

  public AuthFailureHandler(String path) {
    this.path = path;
  }

  @Override
  public void handle(RoutingContext ctx) {
    Throwable e = ctx.failure();

    if (ctx.response().ended()) {
      return;
    }

    if (!(e instanceof AuthenticationException)) {
      ctx.next();
    }

    if (e instanceof AuthenticationSuccessException) {
      ctx.redirect(MessageFormat.format("{0}/success?message={1}", path, e.getMessage()));
    } else {
      ctx.redirect(
          MessageFormat.format(
              "{0}/failure?message={1}&debug={2}",
              path,
              e.getMessage(),
              (e.getCause() != null) ? e.getCause().toString() : e.toString()));
    }
  }
}
