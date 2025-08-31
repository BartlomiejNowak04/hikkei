package dev.tfkls.hikkei.auth;

import static dev.tfkls.hikkei.util.WebTranscoder.url64Decode;
import static java.nio.charset.StandardCharsets.UTF_8;

import dev.tfkls.hikkei.util.WebTranscoder;
import io.mikael.urlbuilder.UrlBuilder;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

public class OAuth2Provider extends AuthenticationProvider {
  public interface Fetcher {
    String fetchUserId(String token, String type, WebClient client);
  }

  public record Specification(
      String name,
      URL authEndpoint,
      URL tokenEndpoint,
      String clientId,
      String clientSecret,
      String permissionScope,
      boolean authByQueryParams,
      Fetcher userIdFetcher) {}

  private final Specification specification;
  private final RandomGenerator generator;
  private final WebClient httpClient;

  protected OAuth2Provider(
      Specification specification,
      URL authRoute,
      RandomGenerator randomGenerator,
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

    var tokenResponse = queryForToken(code).await().bodyAsJsonObject();

    if (tokenResponse.containsKey("error")) {
      throw new IllegalStateException(
          "Failed to verify access code: "
              + tokenResponse.getString("error_description", tokenResponse.getString("error")));
    }
    var token = tokenResponse.getString("access_token");
    var tokenType = tokenResponse.getString("token_type");
    var userId = specification.userIdFetcher.fetchUserId(token, tokenType, httpClient);

    authCallback.onAuthenticate(response, userId);
  }

  private Future<HttpResponse<Buffer>> queryForToken(String code) {
    MultiMap params =
        MultiMap.caseInsensitiveMultiMap()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", getCallbackUrl().toString())
            .add("client_id", specification.clientId);

    HttpRequest<Buffer> query = httpClient.post(specification.tokenEndpoint.toString());
    if (specification.authByQueryParams) {
      params.add("client_secret", specification.clientSecret);
    } else {
      query =
          query.basicAuthentication(
              WebTranscoder.urlEncode(specification.clientId),
              WebTranscoder.urlEncode(specification.clientSecret));
    }
    return query.sendForm(params);
  }

  private void handleRedirectRequest(HttpServerResponse response, String redirectAfterAuthRaw) {
    String redirectAfterAuth = url64Decode(redirectAfterAuthRaw);
    byte[] nonceArray = new byte[48];
    generator.nextBytes(nonceArray);
    String nonce = new String(Base64.getUrlEncoder().encode(nonceArray), UTF_8);
    // TODO: THE REQUESTS SHOULD EXPIRE ON THE BACKEND SIDE!
    if (!Objects.equals(
        UrlBuilder.fromString(redirectAfterAuth).toUrl().getHost(), authRoute.getHost())) {
      throw new IllegalStateException("Auth request redirects outside of host.");
    }
    currentRequests.put(nonce, redirectAfterAuth);

    String oauthRedirect = getAuthenticationUrl(nonce).toString();
    response.setStatusCode(302);
    response.putHeader("Location", oauthRedirect);
    response.end("Redirecting to: " + oauthRedirect);
  }

  private URL getAuthenticationUrl(String nonce) {
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
