package dev.tfkls.hikkei.server.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Optional;

public class AuthFailureHandler implements Handler<RoutingContext> {
  @Override
  public void handle(RoutingContext ctx) {
    Throwable e = ctx.failure();

    String message =
        (e instanceof HttpException)
            ? Optional.ofNullable(((HttpException) e).getPayload())
                .or(() -> Optional.ofNullable(e.getCause()).map(Throwable::getMessage))
                .orElse(e.getMessage())
            : e.getMessage();
    String debugMessage =
        (e instanceof HttpException)
            ? Optional.ofNullable(e.getCause()).orElse(e).toString()
            : e.toString();

    if (ctx.response().ended()) {
      return;
    }

    ctx.redirect(
        MessageFormat.format(
            "/auth/failure?message={0}&debug={1}&status={2}",
            URLEncoder.encode(message, StandardCharsets.UTF_8),
            URLEncoder.encode(debugMessage, StandardCharsets.UTF_8),
            e instanceof HttpException ? ((HttpException) e).getStatusCode() : 500));
  }
}
