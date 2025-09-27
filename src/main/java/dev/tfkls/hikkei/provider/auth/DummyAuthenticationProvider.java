package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthenticationCallback;
import dev.tfkls.hikkei.model.auth.AuthenticationProvider;
import dev.tfkls.hikkei.model.exceptions.AuthenticationFailureException;
import dev.tfkls.hikkei.util.BoundedMap;
import io.vertx.ext.web.RoutingContext;
import java.net.URI;
import java.util.UUID;

public class DummyAuthenticationProvider implements AuthenticationProvider {
  public DummyAuthenticationProvider(URI redirectUri) {
    this.redirectUri = redirectUri;
    this.map = new BoundedMap<>(1000);
  }

  private final URI redirectUri;

  private final BoundedMap<UUID, AuthenticationCallback> map;

  @Override
  public URI requestAuthentication(AuthenticationCallback callback) {
    UUID id = UUID.randomUUID();
    map.put(id, callback);
    return URI.create(redirectUri.toString() + "?state=" + id);
  }

  @Override
  public void handle(RoutingContext ctx) {
    UUID id;
    AuthenticationCallback callback;
    try {
      id = UUID.fromString(ctx.queryParam("state").getFirst());
      callback = map.remove(id).orElseThrow();
      callback.onAuthenticate("dummy", ctx);
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new AuthenticationFailureException("Invalid state URI parameter");
    }
  }
}
