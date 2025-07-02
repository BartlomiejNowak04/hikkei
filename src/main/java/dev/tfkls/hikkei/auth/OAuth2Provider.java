package dev.tfkls.hikkei.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import io.mikael.urlbuilder.UrlBuilder;
import io.mikael.urlbuilder.util.Decoder;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class OAuth2Provider extends AuthenticationProvider {
  public record Specification(
      String name,
      URL authEndpoint,
      String clientId,
      String clientSecret,
      String permissionScope,
      Function<String, String> fetchUserId) {}

  private final Specification specification;
  private final SecureRandom generator;

  protected OAuth2Provider(
      Specification specification, URL authRoute, SecureRandom randomGenerator) {
    super(authRoute);
    this.generator = randomGenerator;
    this.specification = specification;
  }

  private final Map<String, URL> currentRequests = new ConcurrentHashMap<>();

  @Override
  public UrlBuilder getRedirectBuilder(URL redirectAfterAuth) {
    return super.getRedirectBuilder(redirectAfterAuth).addParameter("type", "redirect");
  }

  @Override
  public String getName() {
    return specification.name;
  }

  @Override
  public final void handleRequest(RoutingContext context) {
    String requestType = context.queryParams().get("type");
    HttpServerResponse response = context.response();

    switch (requestType) {
      case "redirect" -> {
        handleRedirectRequest(response, context.queryParams().get("redirect_uri"));
      }
      case "callback" -> {
        throw new RuntimeException("Not yet implemented");
      }
      // TODO: Return a bad query instead of throwing
      case null -> throw new IllegalStateException("Expected a type parameter.");
      default -> throw new IllegalStateException("Invalid type parameter: " + requestType);
    }
  }

  private static final Decoder urlDecoder = new Decoder(UTF_8);

  private void handleRedirectRequest(HttpServerResponse response, String redirectAfterAuthRaw) {
    String nonce = new String(Base64.getUrlEncoder().encode(generator.generateSeed(48)), UTF_8);
    // TODO: THE REQUESTS SHOULD EXPIRE ON THE BACKEND SIDE!
    URL redirectAfterAuth;
    try {
      // TODO: Clean-up a bit so that this handling isn't such a mess.
      redirectAfterAuth = new URI(urlDecoder.urlDecode(redirectAfterAuthRaw, true)).toURL();
    } catch (Exception e) {
      switch (e) {
        case URISyntaxException ex ->
            throw new IllegalStateException("Invalid redirect URI: " + redirectAfterAuthRaw, ex);
        case MalformedURLException ex ->
            throw new IllegalStateException("Invalid redirect URI: " + redirectAfterAuthRaw, ex);
        case RuntimeException ex -> throw ex;
        default -> throw new AssertionError("Unreachable code branch was reached", e);
      }
    }

    currentRequests.put(nonce, redirectAfterAuth);

    String oauthRedirect = getRedirectUrl(nonce).toString();
    response.setStatusCode(302);
    response.putHeader("Location", oauthRedirect);
    response.end("Redirecting to: " + oauthRedirect);
  }

  private URL getRedirectUrl(String nonce) {
    return UrlBuilder.fromUrl(specification.authEndpoint)
        .addParameter("client_id", specification.clientId)
        .addParameter("response_type", "code")
        .addParameter("state", nonce)
        .addParameter(
            "redirect_uri",
            UrlBuilder.fromUrl(authRoute).addParameter("type", "callback").toString())
        .addParameter("scope", specification.permissionScope)
        .toUrl();
  }
}
