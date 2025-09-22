package dev.tfkls.hikkei.server.routes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import io.restassured.http.Method;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class UserRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  @Test
  void byIdNoUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(null);
    testGetException("/api/user/1", new BadDataException("Incorrect parameter: id"));
  }

  @Test
  void byIdDeletedUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(UserData.DELETED);

    JsonObject body = callGetEndpointObject("/api/user/1", 404);
    assertEquals("This user is deleted", body.getString("message"));
  }

  @Test
  void byIdWorkingTest() throws Exception {
    LocalDateTime time = LocalDateTime.now();
    when(userProvider.getUserData(1)).thenReturn(new UserData(1, "aaa", new byte[] {1}, time));

    JsonObject body = callGetEndpointObject("/api/user/1", 200);
    assertEquals(1, body.getInteger("id"));
    assertEquals("aaa", body.getString("username"));
    assertEquals(time.toString(), body.getString("joined"));
    assertArrayEquals(new byte[] {1}, body.getBinary("picture"));
  }

  @Test
  void pfpNoUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(null);
    testGetException("/api/user/pfp/1", new BadDataException("Incorrect parameter: id"));
  }

  @Test
  void pfpDeletedUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(UserData.DELETED);
    testGetException("/api/user/pfp/1", new BadDataException("Incorrect parameter: id"));
  }

  @Test
  void pfpNoPfpTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(new UserData(1, "aaa", null, LocalDateTime.now()));

    callEndpoint("/api/user/pfp/1", Method.GET, null, 204, "");
  }

  @Test
  void pfpWorkingTest() throws Exception {
    when(userProvider.getUserData(1))
        .thenReturn(new UserData(1, "aaa", new byte[0], LocalDateTime.now()));
    when(fileProvider.getContent(new byte[0]))
        .thenReturn(new StoredFile(new byte[] {1, 2, 3}, "image/png"));

    byte[] res = callEndpoint("/api/user/pfp/1", Method.GET, null, 200, "image/png").asByteArray();
    assertArrayEquals(new byte[] {1, 2, 3}, res);
  }

  @Test
  void categoriesNoUserTest() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenReturn(null);
    testGetException("/api/user/categories/1", new BadDataException("Incorrect parameter: id"));
  }

  @Test
  void categoriesEmptyTest() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenReturn(Map.of());
    JsonArray body = callGetEndpointArray("/api/user/categories/1", 200);
    assertEquals(0, body.size());
  }

  @Test
  void categoriesWorkingTest() throws Exception {
    LocalDateTime time = LocalDateTime.now();
    when(userProvider.getCategoriesPermissions(1))
        .thenReturn(
            Map.of(
                1,
                PermissionLevel.ADMIN,
                2,
                PermissionLevel.NO_ACCESS,
                3,
                PermissionLevel.MODERATOR));
    when(categoryProvider.getById(1)).thenReturn(new CategoryData(1, "aa", time));
    when(categoryProvider.getById(2)).thenReturn(null);
    when(categoryProvider.getById(2)).thenReturn(CategoryData.DELETED);
    JsonArray body = callGetEndpointArray("/api/user/categories/1", 200);
    assertEquals(1, body.size());
    assertEquals("aa", body.getJsonObject(0).getString("name"));
    assertEquals(1, body.getJsonObject(0).getInteger("id"));
    assertEquals(time.toString(), body.getJsonObject(0).getString("created"));
    assertEquals(PermissionLevel.ADMIN.toString(), body.getJsonObject(0).getString("permission"));
  }

  @Test
  void createWorkingTest() throws Exception {
    when(userProvider.createUser(any())).thenReturn(1);
    JsonObject body =
        callEndpointObject(
            "/api/user/create", Method.POST, new JsonObject().put("name", "aaa"), 201);
    assertEquals(1, body.getInteger("id"));
  }

  // TODO: mock session and uncomment
  //  @Test
  //  void updateNameWorkingTest() {
  //    callEndpoint(
  //        "/api/user/update/name",
  //        Method.PUT,
  //        new JsonObject().put("name", "aaa"),
  //        204,
  //        "");
  //  }
  //
  //  @Test
  //  void updatePfpNoIdTest() {
  //    testException(
  //        "/api/user/update/pfp",
  //        Method.PUT,
  //        null,
  //        new BadDataException("Missing required parameter: id"));
  //  }
  //
  //  @Test
  //  void updatePfpInvalidUserTest() {
  //    given()
  //        .multiPart("id", "aaa")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(400)
  //        .body("message", equalTo("Incorrect parameter: id"));
  //  }
  //
  //  @Test
  //  void updatePfpNoFileTest() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenReturn(true);
  //    given()
  //        .multiPart("id", 1)
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(400)
  //        .body("message", equalTo("Exactly one file must be supplied"));
  //  }
  //
  //  @Test
  //  void updatePfpTwoFilesTest() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenReturn(true);
  //    given()
  //        .multiPart("id", 1)
  //        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}),
  // "image/png")
  //        .multiPart(
  //            "file", "mock2.jpg", new ByteArrayInputStream(new byte[] {1, 2, 3, 4}), "image/jpg")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(400)
  //        .body("message", equalTo("Exactly one file must be supplied"));
  //  }
  //
  //  @Test
  //  void updatePfpInvalidFiletypeTest() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenReturn(true);
  //    given()
  //        .multiPart("id", 1)
  //        .multiPart(
  //            "file", "mock.pdf", new ByteArrayInputStream(new byte[] {1, 2, 3}),
  // "application/pdf")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(400)
  //        .body("message", equalTo("Unsupported image type"));
  //  }
  //
  //  @Test
  //  void updatePfpWorkingTest() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenReturn(true);
  //    given()
  //        .multiPart("id", 1)
  //        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}),
  // "image/png")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(204);
  //  }
  //
  //  @Test
  //  void deleteWorkingTest() {
  //    callEndpoint("/api/user/delete", Method.DELETE, id1, 204, "");
  //  }
}
