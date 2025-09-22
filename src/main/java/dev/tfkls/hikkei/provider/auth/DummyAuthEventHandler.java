package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.user.AuthData;
import io.vertx.ext.web.RoutingContext;

public class DummyAuthEventHandler implements AuthEventHandler {

  @Override
  public void onLogin(AuthData authData, RoutingContext ctx) {
    ctx.redirect("/auth/success");
  }

  @Override
  public void onRegister(AuthData authData, RoutingContext ctx) {
    ctx.redirect("/auth/success");
  }

  @Override
  public void onMerge(AuthData authData, RoutingContext ctx) {
    ctx.redirect("/auth/success");
  }
}
