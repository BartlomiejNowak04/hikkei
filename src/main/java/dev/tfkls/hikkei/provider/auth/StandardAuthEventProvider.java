package dev.tfkls.hikkei.provider.auth;

import dev.tfkls.hikkei.model.auth.AuthEventHandler;
import dev.tfkls.hikkei.model.auth.SessionProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import io.vertx.ext.web.RoutingContext;

public class StandardAuthEventProvider implements AuthEventHandler {
  private final UserDataProvider userDataProvider;
  private final SessionProvider sessionProvider;

  StandardAuthEventProvider(UserDataProvider userDataProvider, SessionProvider sessionProvider) {
    this.userDataProvider = userDataProvider;
    this.sessionProvider = sessionProvider;
  }

  @Override
  public void onLogin(String providerName, String token, RoutingContext ctx) {}

  @Override
  public void onRegister(String providerName, String token, RoutingContext ctx) {}

  @Override
  public void onMerge(String providerName, String token, RoutingContext ctx) {}
}
