package dev.tfkls.hikkei.auth;

import static java.nio.charset.StandardCharsets.UTF_8;

import dev.tfkls.hikkei.util.WebTranscoder;
import io.mikael.urlbuilder.UrlBuilder;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class OAuth2Provider extends AuthenticationProvider {
  public record Specification(
      String name,
      URL authEndpoint,
      URL tokenEndpoint,
      String clientId,
      String clientSecret,
      String permissionScope,
      Function<String, String> fetchUserId) {}

  private final Specification specification;
  private final SecureRandom generator;
  private final WebClient httpClient;

  protected OAuth2Provider(
      Specification specification,
      URL authRoute,
      SecureRandom randomGenerator,
      WebClient httpClient) {
    super(authRoute);

    this.generator = randomGenerator;
    this.specification = specification;
    this.httpClient = httpClient;
  }

  private final Map<String, String> currentRequests = new ConcurrentHashMap<>();

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
      case "redirect" -> handleRedirectRequest(response, context.queryParams().get("redirect_uri"));
      case "callback" -> handleCallbackRequest(response, context.queryParams());

      // TODO: Return a bad query instead of throwing
      case null -> throw new IllegalStateException("Expected a type parameter.");
      default -> throw new IllegalStateException("Invalid type parameter: " + requestType);
    }
  }

  private void handleCallbackRequest(HttpServerResponse response, MultiMap params) {
    String state = params.get("state");
    if (state == null) {
      throw new IllegalStateException("Expected a state parameter.");
    } else if (!currentRequests.containsKey(state)) {
      throw new IllegalStateException(
          "Invalid state parameter received; authentication request may have already expired.");
    } else if (params.get("error") != null) {
      throw new IllegalStateException(
          "Failed authorization request: "
              + Optional.ofNullable(params.get("error_description")).orElse(params.get("error")));
    } else if (params.get("code") == null) {
      throw new IllegalStateException("Expected a code parameter.");
    }

    String code = params.get("code");

    var tokenResponse =
        httpClient
            .post(specification.tokenEndpoint.toString())
            .basicAuthentication(
                WebTranscoder.urlEncode(specification.clientId),
                WebTranscoder.urlEncode(specification.clientSecret))
            .sendForm(
                MultiMap.caseInsensitiveMultiMap()
                    .add("grant_type", "authorization_code")
                    .add("code", code)
                    .add("redirect_uri", getCallbackUrl().toString())
                    .add("client_id", specification.clientId)
                    .add("client_secret", specification.clientSecret))
            .await();

    throw new RuntimeException("Not yet implemented");
  }

  private void handleRedirectRequest(HttpServerResponse response, String redirectAfterAuth) {
    String nonce = new String(Base64.getUrlEncoder().encode(generator.generateSeed(48)), UTF_8);
    // TODO: THE REQUESTS SHOULD EXPIRE ON THE BACKEND SIDE!
    if (!Objects.equals(
        UrlBuilder.fromString(redirectAfterAuth).toUrl().getHost(), authRoute.getHost())) {
      throw new IllegalStateException("Auth request redirects outside of host.");
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
        .addParameter("redirect_uri", getCallbackUrl().toString())
        .addParameter("scope", specification.permissionScope)
        .toUrl();
  }

  private URL getCallbackUrl() {
    return UrlBuilder.fromUrl(authRoute).addParameter("type", "callback").toUrl();
  }
}
