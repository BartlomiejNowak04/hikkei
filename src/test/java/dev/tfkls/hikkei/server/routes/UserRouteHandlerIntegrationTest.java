package dev.tfkls.hikkei.server.routes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import io.restassured.http.Method;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UserRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private JsonObject id1;
  private JsonObject user1;

  @BeforeAll
  void init() {
    id1 = new JsonObject().put("id", 1);
    user1 = new JsonObject().put("user", 1);
  }

  @Test
  void byIdNoUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(null);

    JsonObject body = callEndpointObject("/api/user/byid", Method.POST, id1, 400);
    assertEquals("Incorrect parameter: id", body.getString("message"));
  }

  @Test
  void byIdDeletedUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(UserData.DELETED);

    JsonObject body = callEndpointObject("/api/user/byid", Method.POST, id1, 404);
    assertEquals("This user is deleted", body.getString("message"));
  }

  @Test
  void byIdWorkingTest() throws Exception {
    LocalDateTime time = LocalDateTime.now();
    when(userProvider.getUserData(1)).thenReturn(new UserData(1, "aaa", new byte[] {1}, time));

    JsonObject body = callEndpointObject("/api/user/byid", Method.POST, id1, 200);
    assertEquals(1, body.getInteger("id"));
    assertEquals("aaa", body.getString("username"));
    assertEquals(time.toString(), body.getString("joined"));
    assertArrayEquals(new byte[] {1}, body.getBinary("picture"));
  }

  @Test
  void pfpNoUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(null);

    JsonObject body = callEndpointObject("/api/user/pfp", Method.POST, id1, 400);
    assertEquals("Incorrect parameter: id", body.getString("message"));
  }

  @Test
  void pfpDeletedUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(UserData.DELETED);

    JsonObject body = callEndpointObject("/api/user/pfp", Method.POST, id1, 400);
    assertEquals("Incorrect parameter: id", body.getString("message"));
  }

  @Test
  void pfpNoPfpTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(new UserData(1, "aaa", null, LocalDateTime.now()));

    callEndpoint("/api/user/pfp", Method.POST, id1, 204, "");
  }

  @Test
  void pfpWorkingTest() throws Exception {
    when(userProvider.getUserData(1))
        .thenReturn(new UserData(1, "aaa", new byte[0], LocalDateTime.now()));
    when(fileProvider.getContent(new byte[0]))
        .thenReturn(new StoredFile(new byte[] {1, 2, 3}, "image/png"));

    byte[] res = callEndpoint("/api/user/pfp", Method.POST, id1, 200, "image/png").asByteArray();
    assertArrayEquals(new byte[] {1, 2, 3}, res);
  }

  @Test
  void categoriesNoUserTest() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenReturn(null);
    JsonObject body = callEndpointObject("/api/user/categories", Method.POST, user1, 400);
    assertEquals("Incorrect parameter: user", body.getString("message"));
  }

  @Test
  void categoriesEmptyTest() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenReturn(Map.of());
    JsonArray body = callEndpointArray("/api/user/categories", Method.POST, user1, 200);
    assertEquals(0, body.size());
  }

  @Test
  void categoriesWorkingTest() throws Exception {
    LocalDateTime time = LocalDateTime.now();
    when(userProvider.getCategoriesPermissions(1))
        .thenReturn(
            Map.of(
                1,
                PermissionLevel.Admin,
                2,
                PermissionLevel.NoAccess,
                3,
                PermissionLevel.Moderator));
    when(categoryProvider.getById(1)).thenReturn(new CategoryData(1, "aa", time));
    when(categoryProvider.getById(2)).thenReturn(null);
    when(categoryProvider.getById(2)).thenReturn(CategoryData.DELETED);
    JsonArray body = callEndpointArray("/api/user/categories", Method.POST, user1, 200);
    assertEquals(1, body.size());
    assertEquals("aa", body.getJsonObject(0).getString("name"));
    assertEquals(1, body.getJsonObject(0).getInteger("id"));
    assertEquals(time.toString(), body.getJsonObject(0).getString("created"));
    assertEquals(PermissionLevel.Admin.toString(), body.getJsonObject(0).getString("permission"));
  }

  @Test
  void createWorkingTest() throws Exception {
    when(userProvider.createUser(any())).thenReturn(1);
    JsonObject body =
        callEndpointObject(
            "/api/user/create", Method.PUT, new JsonObject().put("name", "aaa"), 201);
    assertEquals(1, body.getInteger("id"));
  }

  @Test
  void updateNameWorkingTest() {
    callEndpoint(
        "/api/user/update/name",
        Method.PUT,
        new JsonObject().put("name", "aaa").put("id", 1),
        204,
        "");
  }

  @Test
  void updatePfpNoIdTest() {
    JsonObject body = callEndpointObject("/api/user/update/pfp", Method.PUT, null, 400);
    assertEquals("Missing required parameter: id", body.getString("message"));
  }

  @Test
  void updatePfpInvalidUserTest() {
    given()
        .multiPart("id", "aaa")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter: id"));
  }

  @Test
  void updatePfpNoFileTest() throws Exception {
    when(userProvider.doesUserExist(1)).thenReturn(true);
    given()
        .multiPart("id", 1)
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(400)
        .body("message", equalTo("Exactly one file must be supplied"));
  }

  @Test
  void updatePfpTwoFilesTest() throws Exception {
    when(userProvider.doesUserExist(1)).thenReturn(true);
    given()
        .multiPart("id", 1)
        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}), "image/png")
        .multiPart(
            "file", "mock2.jpg", new ByteArrayInputStream(new byte[] {1, 2, 3, 4}), "image/jpg")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(400)
        .body("message", equalTo("Exactly one file must be supplied"));
  }

  @Test
  void updatePfpInvalidFiletypeTest() throws Exception {
    when(userProvider.doesUserExist(1)).thenReturn(true);
    given()
        .multiPart("id", 1)
        .multiPart(
            "file", "mock.pdf", new ByteArrayInputStream(new byte[] {1, 2, 3}), "application/pdf")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(400)
        .body("message", equalTo("Unsupported image type"));
  }

  @Test
  void updatePfpWorkingTest() throws Exception {
    when(userProvider.doesUserExist(1)).thenReturn(true);
    given()
        .multiPart("id", 1)
        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}), "image/png")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(204);
  }

  @Test
  void updatePfpBadDataTest() throws Exception {
    when(userProvider.doesUserExist(1)).thenReturn(true);
    doThrow(new BadDataException("test")).when(userProvider).updateUserPicture(anyInt(), any());
    given()
        .multiPart("id", 1)
        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}), "image/png")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(400)
        .body("message", equalTo("test"));
  }

  @Test
  void updatePfpInternalErrorTest() throws Exception {
    when(userProvider.doesUserExist(1)).thenThrow(new InternalErrorException("test"));
    given()
        .multiPart("id", 1)
        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}), "image/png")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(500)
        .body("message", equalTo("test"));
  }

  @Test
  void updatePfpInternalErrorTest2() throws Exception {
    when(userProvider.doesUserExist(1)).thenReturn(true);
    doThrow(new InternalErrorException("test"))
        .when(userProvider)
        .updateUserPicture(anyInt(), any());
    given()
        .multiPart("id", 1)
        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}), "image/png")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(500)
        .body("message", equalTo("test"));
  }

  @Test
  void updatePfpInternalErrorTest3() throws Exception {
    when(userProvider.doesUserExist(1)).thenReturn(true);
    doThrow(new InternalErrorException("test")).when(fileProvider).storeContent(any(), any());
    given()
        .multiPart("id", 1)
        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}), "image/png")
        .when()
        .put("/api/user/update/pfp")
        .then()
        .statusCode(500)
        .body("message", equalTo("test"));
  }

  @Test
  void deleteWorkingTest() {
    callEndpoint("/api/user/delete", Method.DELETE, id1, 204, "");
  }
}
