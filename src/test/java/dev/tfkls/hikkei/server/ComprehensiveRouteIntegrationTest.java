package dev.tfkls.hikkei.server;

import static dev.tfkls.hikkei.server.util.RestCaller.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import dev.tfkls.hikkei.provider.JDBC.*;
import dev.tfkls.hikkei.provider.JDBCIntegrationTest;
import dev.tfkls.hikkei.provider.auth.TokenAuthenticationProvider;
import dev.tfkls.hikkei.provider.filesystem.FilesystemFileProvider;
import dev.tfkls.hikkei.provider.filesystem.FilesystemVersionProvider;
import dev.tfkls.hikkei.provider.page.MarkdownPageRenderer;
import io.restassured.RestAssured;
import io.restassured.http.Cookie;
import io.restassured.http.Method;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComprehensiveRouteIntegrationTest extends JDBCIntegrationTest {
  static final String TOKEN = "jaSI1Sjh4A_41jnEsx9";

  @BeforeAll
  void initServer(Vertx vertx, VertxTestContext testContext) throws Exception {
    Integer port = Integer.valueOf(require("PORT"));
    RestAssured.baseURI = "http://localhost";
    RestAssured.port = port;

    UserDataProvider userDataProvider = new JDBCUserDataProvider(connection);
    int adminId =
        userDataProvider.createUser(
            new UserData(null, "admin", null, null, PermissionLevel.SUPER_ADMIN));
    userDataProvider.addUserAuthMethod(adminId, new AuthData("admin", ""));

    new File("./.testfs/version").mkdirs();
    new File("./.testfs/file").mkdirs();

    FileProvider fileProvider = new FilesystemFileProvider("./.testfs/file");

    vertx
        .deployVerticle(
            new MainVerticle(
                userDataProvider,
                new JDBCPageDataProvider(connection),
                new JDBCRevisionDataProvider(connection),
                new JDBCCommentDataProvider(connection),
                new JDBCCategoryDataProvider(connection),
                new JDBCBlobDataProvider(connection, fileProvider),
                new FilesystemVersionProvider("./.testfs/version"),
                fileProvider,
                port,
                URI.create(String.format("%s:%d", RestAssured.baseURI, port)),
                Map.of("admin", uri -> new TokenAuthenticationProvider(TOKEN, uri)),
                new MarkdownPageRenderer()))
        .onComplete(testContext.succeedingThenComplete());
  }

  @Test
  public void comprehensiveTest() {
    JsonArray receivedMethods = callEndpointArray("/api/auth/methods", Method.GET, null, 200);
    assertEquals(JsonArray.of("admin"), receivedMethods);

    String htmlForm = callEndpoint("/api/auth/login/admin", Method.GET, null, 200, "").asString();

    Pattern uuidPattern = Pattern.compile("value='(?<uuid>[0-9a-f-]*)'");
    Matcher uuidMatcher = uuidPattern.matcher(htmlForm);
    assertTrue(uuidMatcher.find());
    UUID stateUuid = UUID.fromString(uuidMatcher.group("uuid"));

    var oldRedirectConfig = RestAssured.config().getRedirectConfig();

    String sessionId =
        given()
            .redirects()
            .follow(false)
            .when()
            .body("")
            .request(Method.GET, "/api/auth/callback/admin?state=" + stateUuid + "&token=" + TOKEN)
            .then()
            .statusCode(302)
            .cookie("HikkeiSession")
            .extract()
            .cookie("HikkeiSession");

    Cookie sessionCookie = new Cookie.Builder("HikkeiSession", sessionId).build();

    JsonObject userData =
        new JsonObject(
            callEndpoint("/api/user/", Method.GET, null, 200, "application/json", sessionCookie)
                .asString());

    int userId = userData.getInteger("id");

    int categoryId =
        new JsonObject(
                callEndpoint(
                        "/api/category/create",
                        Method.POST,
                        JsonObject.of("name", "Cats & Dogs"),
                        201,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getInteger("id");

    int pageId =
        new JsonObject(
                callEndpoint(
                        "/api/page/create",
                        Method.POST,
                        JsonObject.of("category", categoryId, "title", "Kitty"),
                        201,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getInteger("id");

    int revisionId =
        new JsonObject(
                callEndpoint(
                        "/api/revision/create",
                        Method.POST,
                        JsonObject.of(
                            "page_id",
                            pageId,
                            "content",
                            "**A quick *brown* fox jumped over the lazy** dog."),
                        201,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getInteger("id");

    callEndpoint(
        "/api/revision/page/" + pageId, Method.GET, null, 200, "application/json", sessionCookie);

    callEndpoint(
        "/api/revision/update/status",
        Method.PUT,
        JsonObject.of("id", revisionId, "status", true),
        204,
        "",
        sessionCookie);

    callEndpoint(
        "/api/revision/page/" + pageId, Method.GET, null, 200, "application/json", sessionCookie);

    String renderedContent =
        new JsonObject(
                callEndpoint(
                        "/api/revision/current/" + pageId + "/rendered",
                        Method.GET,
                        null,
                        200,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getString("content");

    assertEquals(
        "<p><strong>A quick <em>brown</em> fox jumped over the lazy</strong> dog.</p>\n",
        renderedContent);

    String rawContent =
        new JsonObject(
                callEndpoint(
                        "/api/revision/current/" + pageId + "/raw",
                        Method.GET,
                        null,
                        200,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getString("content");

    assertEquals("**A quick *brown* fox jumped over the lazy** dog.", rawContent);

    String renderedContent2 =
        new JsonObject(
                callEndpoint(
                        "/api/revision/" + revisionId + "/rendered",
                        Method.GET,
                        null,
                        200,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getString("content");

    assertEquals(renderedContent, renderedContent2);

    String rawContent2 =
        new JsonObject(
                callEndpoint(
                        "/api/revision/" + revisionId + "/raw",
                        Method.GET,
                        null,
                        200,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getString("content");

    assertEquals(rawContent, rawContent2);

    String renderedContent3 =
        new JsonObject(
                callEndpoint(
                        "/api/revision/render",
                        Method.POST,
                        JsonObject.of("content", rawContent),
                        200,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getString("content");

    assertEquals(renderedContent, renderedContent3);

    JsonObject exampleJson =
        new JsonObject()
            .put("test", 1234)
            .put("test1", 123.4)
            .put("test2", "test2")
            .put("test3", new JsonArray().add(1).add("2").add(JsonObject.of("3", 4.5)));

    int blobId =
        new JsonObject(
                callEndpoint(
                        "/api/blob/create",
                        Method.POST,
                        exampleJson,
                        201,
                        "application/json",
                        sessionCookie)
                    .asString())
            .getInteger("id");

    String blobData =
        callEndpoint(
                "/api/blob/" + blobId + "/data",
                Method.GET,
                null,
                200,
                "application/json",
                sessionCookie)
            .asString();

    assertEquals(exampleJson.toString(), blobData);

    JsonObject blobMetadata =
        new JsonObject(
            callEndpoint(
                    "/api/blob/" + blobId + "/meta",
                    Method.GET,
                    null,
                    200,
                    "application/json",
                    sessionCookie)
                .asString());

    assertEquals(blobData.length(), blobMetadata.getInteger("size"));
    assertEquals("application/json", blobMetadata.getString("mime"));
    assertEquals(userId, blobMetadata.getInteger("user_id"));
    assertEquals(blobId, blobMetadata.getInteger("id"));

    callEndpoint(
        "/api/blob/delete", Method.DELETE, JsonObject.of("id", blobId), 204, "", sessionCookie);

    callEndpoint("/api/blob/" + blobId + "/data", Method.GET, null, 404, "", sessionCookie);
  }
}
