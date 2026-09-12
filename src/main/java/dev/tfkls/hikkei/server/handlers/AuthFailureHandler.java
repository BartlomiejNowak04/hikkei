package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.exceptions.AuthenticationException;
import dev.tfkls.hikkei.model.exceptions.AuthenticationSuccessException;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
      // Not an auth-flow outcome: let the generic failure handler answer.
      ctx.next();
      return;
    }

    if (e instanceof AuthenticationSuccessException) {
      ctx.redirect(path + "/success?message=" + encode(e.getMessage()));
    } else {
      String debug = (e.getCause() != null) ? e.getCause().toString() : e.toString();
      ctx.redirect(path + "/failure?message=" + encode(e.getMessage()) + "&debug=" + encode(debug));
    }
  }

  private static String encode(String value) {
    return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
  }
}
