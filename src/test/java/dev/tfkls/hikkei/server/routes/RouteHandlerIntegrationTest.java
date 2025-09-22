package dev.tfkls.hikkei.server.routes;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.VersionProvider;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.server.MainVerticle;
import dev.tfkls.hikkei.test.IntegrationTest;
import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.net.URI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class RouteHandlerIntegrationTest extends IntegrationTest {
  protected UserDataProvider userProvider;
  protected PageDataProvider pageProvider;
  protected RevisionDataProvider revisionProvider;
  protected CommentDataProvider commentProvider;
  protected CategoryDataProvider categoryProvider;
  protected VersionProvider versionProvider;
  protected FileProvider fileProvider;

  @BeforeAll
  void initServer(Vertx vertx, VertxTestContext testContext) {
    Integer port = Integer.valueOf(require("PORT"));
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;

    userProvider = mock();
    pageProvider = mock();
    revisionProvider = mock();
    commentProvider = mock();
    categoryProvider = mock();
    versionProvider = mock();
    fileProvider = mock();

    vertx
        .deployVerticle(
            new MainVerticle(
                userProvider,
                pageProvider,
                revisionProvider,
                commentProvider,
                categoryProvider,
                versionProvider,
                fileProvider,
                port,
                URI.create(String.format("%s:%d", RestAssured.baseURI, port)),
                null))
        .onComplete(testContext.succeedingThenComplete());
  }

  @BeforeEach
  void resetMocks() {
    Mockito.reset(
        userProvider,
        pageProvider,
        revisionProvider,
        commentProvider,
        categoryProvider,
        versionProvider,
        fileProvider);
  }

  protected ResponseBodyExtractionOptions callEndpoint(
      String endpoint, Method method, JsonObject body, int statusCode, String contentType) {
    return given()
        .when()
        .body(body == null ? "" : body.toString())
        .request(method, endpoint)
        .then()
        .statusCode(statusCode)
        .contentType(contentType)
        .extract()
        .body();
  }

  protected JsonObject callEndpointObject(
      String endpoint, Method method, JsonObject body, int statusCode) {
    return new JsonObject(
        callEndpoint(endpoint, method, body, statusCode, "application/json").asString());
  }

  protected JsonObject callGetEndpointObject(String endpoint, int statusCode) {
    return callEndpointObject(endpoint, Method.GET, null, statusCode);
  }

  protected JsonArray callEndpointArray(
      String endpoint, Method method, JsonObject body, int statusCode) {
    return new JsonArray(
        callEndpoint(endpoint, method, body, statusCode, "application/json").asString());
  }

  protected JsonArray callGetEndpointArray(String endpoint, int statusCode) {
    return callEndpointArray(endpoint, Method.GET, null, statusCode);
  }

  protected void testException(String endpoint, Method method, JsonObject body, Exception e) {
    JsonObject bd =
        callEndpointObject(endpoint, method, body, e instanceof InternalErrorException ? 500 : 400);
    assertEquals(e.getMessage(), bd.getString("message"));
  }

  protected void testGetException(String endpoint, Exception e) {
    testException(endpoint, Method.GET, null, e);
  }
}
