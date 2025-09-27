package dev.tfkls.hikkei.server.handlers;

import dev.tfkls.hikkei.model.auth.AccessManager;
import dev.tfkls.hikkei.server.util.LogFunction;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import java.text.MessageFormat;
import java.util.Optional;

public class LogHandler implements Handler<RoutingContext> {
  private final LogFunction logFunction;
  private final AccessManager accessManager;

  public LogHandler(LogFunction logFunction, AccessManager accessManager) {
    this.logFunction = logFunction;
    this.accessManager = accessManager;
  }

  public void logResponse(RoutingContext ctx, String version, String method, String uri) {
    Optional<String> user = accessManager.fetchUser(ctx).map(String::valueOf);
    int statusCode = ctx.response().getStatusCode();
    String statusMessage = ctx.response().getStatusMessage();

    logFunction.log(
        MessageFormat.format(
            "{0} {1} {2} {3} – got {4} {5}\n",
            version,
            method,
            uri,
            user.map(s -> "authenticated as " + s).orElse("unauthenticated"),
            statusCode,
            statusMessage));
  }

  @Override
  public void handle(RoutingContext ctx) {
    HttpServerRequest request = ctx.request();

    String uri = request.uri();
    String method = request.method().name();
    String version =
        switch (request.version()) {
          case HTTP_1_0 -> "HTTP/1.0";
          case HTTP_1_1 -> "HTTP/1.1";
          case HTTP_2 -> "HTTP/2.0";
          case null -> "HTTP/Unknown";
        };

    ctx.addHeadersEndHandler((v) -> this.logResponse(ctx, version, method, uri));
    ctx.next();
  }
}
