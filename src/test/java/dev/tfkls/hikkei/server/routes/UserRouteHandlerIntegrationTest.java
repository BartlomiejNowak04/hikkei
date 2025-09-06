package dev.tfkls.hikkei.server.routes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.model.user.UserData;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

public class UserRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  @Test
  void byIdNoUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(null);

    given()
        .body(new JsonObject().put("id", 1).toString())
        .when()
        .post("/api/user/byid")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter: id"));
  }

  @Test
  void byIdDeletedUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(UserData.DELETED);

    given()
        .body(new JsonObject().put("id", 1).toString())
        .when()
        .post("/api/user/byid")
        .then()
        .statusCode(404)
        .body("message", equalTo("This user is deleted"));
  }

  @Test
  void byIdWorkingTest() throws Exception {
    LocalDateTime time = LocalDateTime.now();
    when(userProvider.getUserData(1)).thenReturn(new UserData(1, "aaa", new byte[] {1}, time));

    String body =
        given()
            .body(new JsonObject().put("id", 1).toString())
            .when()
            .post("/api/user/byid")
            .then()
            .statusCode(200)
            .body("id", equalTo(1))
            .body("username", equalTo("aaa"))
            .body("joined", equalTo(time.toString()))
            .extract()
            .body()
            .asPrettyString();

    assertArrayEquals(new byte[] {1}, new JsonObject(body).getBinary("picture"));
  }

  @Test
  void pfpNoUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(null);

    given()
        .body(new JsonObject().put("id", 1).toString())
        .when()
        .post("/api/user/pfp")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter: id"));
  }

  @Test
  void pfpDeletedUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(UserData.DELETED);

    given()
        .body(new JsonObject().put("id", 1).toString())
        .when()
        .post("/api/user/pfp")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter: id"));
  }

  @Test
  void pfpNoPfpTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(new UserData(1, "aaa", null, LocalDateTime.now()));

    given()
        .body(new JsonObject().put("id", 1).toString())
        .when()
        .post("/api/user/pfp")
        .then()
        .statusCode(204);
  }

  @Test
  void pfpWorkingTest() throws Exception {
    when(userProvider.getUserData(1))
        .thenReturn(new UserData(1, "aaa", new byte[0], LocalDateTime.now()));
    when(fileProvider.getContent(new byte[0]))
        .thenReturn(new StoredFile(new byte[] {1, 2, 3}, "image/png"));

    byte[] res =
        given()
            .body(new JsonObject().put("id", 1).toString())
            .when()
            .post("/api/user/pfp")
            .then()
            .statusCode(200)
            .contentType("image/png")
            .extract()
            .body()
            .asByteArray();
    assertArrayEquals(new byte[] {1, 2, 3}, res);
  }

  @Test
  void userCreateWorkingTest() throws Exception {
    when(userProvider.createUser(any())).thenReturn(1);

    given()
        .body(new JsonObject().put("name", "aaa").toString())
        .when()
        .put("/api/user/create")
        .then()
        .statusCode(201)
        .body("id", equalTo(1));
  }
}
