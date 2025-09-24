package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthenticationCallback;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.util.BoundedMap;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.HttpException;
import java.net.URI;
import java.util.NoSuchElementException;
import java.util.UUID;

public class TokenAuthenticationProvider implements AuthenticationProvider {
  private final String token;
  private final URI callbackUri;

  public TokenAuthenticationProvider(String token, URI callbackUri) {
    this.token = token;
    this.callbackUri = callbackUri;
    this.authRequests = new BoundedMap<>(1000);
  }

  private final BoundedMap<UUID, AuthenticationCallback> authRequests;

  @Override
  public URI requestAuthentication(AuthenticationCallback callback) {
    UUID uuid = UUID.randomUUID();
    authRequests.put(uuid, callback);
    return URI.create(callbackUri.toString() + "?state=" + uuid);
  }

  @Override
  public void handle(RoutingContext ctx) {
    if (ctx.queryParam("state").isEmpty()) {
      throw new HttpException(400, "Missing the state parameter");
    }
    String state = ctx.queryParam("state").getFirst();
    AuthenticationCallback callback;
    try {
      callback = authRequests.get(UUID.fromString(state)).orElseThrow();
    } catch (IllegalArgumentException | NoSuchElementException err) {
      throw new HttpException(400, "Invalid state parameter");
    }

    if (!ctx.queryParam("token").isEmpty()) {
      String token = ctx.queryParam("token").getFirst();
      if (token.equals(this.token)) {
        callback.onAuthenticate("", ctx);
      } else {
        throw new HttpException(403, "Invalid access token");
      }
    } else {
      ctx.response().setStatusCode(200);
      ctx.response().putHeader("content-type", "text/html");
      ctx.end(
          """
        <!DOCTYPE html>
        <html>
        <body>

        <h1>Login with access token</h1>
        <form action='"""
              + callbackUri.toString()
              + """
        ' method="get">
        <label for="token">Enter access token:</label>
        <input type="text" id="token" name="token"><br><br>
        <input type="hidden" id="state" name="state" value='"""
              + state
              + """
        '>
        <input type="submit" value="Submit">
        </form>

        </body>
        </html>
        """);
    }
  }
}
