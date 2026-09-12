package dev.tfkls.hikkei.server.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.exceptions.AuthenticationFailureException;
import dev.tfkls.hikkei.model.exceptions.AuthenticationSuccessException;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuthFailureHandlerTest {
  private RoutingContext contextFailingWith(Throwable failure) {
    var ctx = mock(RoutingContext.class);
    var response = mock(HttpServerResponse.class);
    when(ctx.failure()).thenReturn(failure);
    when(ctx.response()).thenReturn(response);
    when(response.ended()).thenReturn(false);
    return ctx;
  }

  @Test
  void nonAuthFailureIsDelegatedAndNotRedirected() {
    var ctx = contextFailingWith(new IllegalStateException("boom"));

    new AuthFailureHandler("/auth").handle(ctx);

    verify(ctx).next();
    verify(ctx, never()).redirect(any());
  }

  @Test
  void endedResponseIsLeftAlone() {
    var ctx = contextFailingWith(new AuthenticationSuccessException("done"));
    when(ctx.response().ended()).thenReturn(true);

    new AuthFailureHandler("/auth").handle(ctx);

    verify(ctx, never()).next();
    verify(ctx, never()).redirect(any());
  }

  @Test
  void successRedirectsWithEncodedMessage() {
    var ctx = contextFailingWith(new AuthenticationSuccessException("Logged in as bob & co"));

    new AuthFailureHandler("/auth").handle(ctx);

    ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
    verify(ctx).redirect(location.capture());
    verify(ctx, never()).next();
    assertEquals("/auth/success?message=Logged+in+as+bob+%26+co", location.getValue());
  }

  @Test
  void failureRedirectsWithEncodedMessageAndDebug() {
    var cause = new IllegalArgumentException("state=1&code=2#frag");
    var ctx = contextFailingWith(new AuthenticationFailureException("Invalid state", cause));

    new AuthFailureHandler("/auth").handle(ctx);

    ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
    verify(ctx).redirect(location.capture());
    assertEquals(
        "/auth/failure?message=Invalid+state&debug="
            + "java.lang.IllegalArgumentException%3A+state%3D1%26code%3D2%23frag",
        location.getValue());
  }

  @Test
  void failureWithoutCauseUsesExceptionAsDebug() {
    var failure = new AuthenticationFailureException("Invalid access token");
    var ctx = contextFailingWith(failure);

    new AuthFailureHandler("/auth").handle(ctx);

    ArgumentCaptor<String> location = ArgumentCaptor.forClass(String.class);
    verify(ctx).redirect(location.capture());
    assertEquals(
        "/auth/failure?message=Invalid+access+token&debug="
            + "dev.tfkls.hikkei.model.exceptions.AuthenticationFailureException"
            + "%3A+Invalid+access+token",
        location.getValue());
  }
}
