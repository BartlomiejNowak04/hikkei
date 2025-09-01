package dev.tfkls.hikkei.server.routes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CommentRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  @Test
  void testGetByUserReturnsComments() throws Exception {
    // given
    CommentData comment1 = new CommentData(1, 10, "First comment", null, false);
    CommentData comment2 = new CommentData(2, 10, "Second comment", null, false);

    when(commentProvider.getByUser(42)).thenReturn(List.of(comment1, comment2));

    given()
        .contentType("application/json")
        .body("{\"user\": 42}")
        .when()
        .post("/api/comment/user")
        .then()
        .statusCode(200)
        .body("$", hasSize(2))
        .body("[0].id", equalTo(1))
        .body("[0].page_id", equalTo(10))
        .body("[0].content", equalTo("First comment"))
        .body("[0].date", equalTo(null))
        .body("[0].deleted", equalTo(false))
        .body("[1].id", equalTo(2))
        .body("[1].page_id", equalTo(10))
        .body("[1].content", equalTo("Second comment"))
        .body("[1].date", equalTo(null))
        .body("[1].deleted", equalTo(false));

    verify(commentProvider, times(1)).getByUser(42);
  }

  @Test
  void testGetByUserMissingParameterReturns400() {
    given()
        .contentType("application/json")
        .body("{\"wrongKey\": 42}")
        .when()
        .post("/api/comment/user")
        .then()
        .statusCode(400)
        .body("message", equalTo("Missing required parameter: user"));

    verifyNoInteractions(commentProvider);
  }

  @Test
  void testGetByUserNullParameterReturns400() {
    given()
        .contentType("application/json")
        .body("{\"user\": null}")
        .when()
        .post("/api/comment/user")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter: user"));

    verifyNoInteractions(commentProvider);
  }

  @Test
  void testGetByUserWrongTypeParameterReturns400() {
    given()
        .contentType("application/json")
        .body("{\"user\": \"notAnInteger\"}")
        .when()
        .post("/api/comment/user")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter: user"));

    verifyNoInteractions(commentProvider);
  }

  @Test
  void testGetByUserNullResponseReturns400() throws Exception {
    when(commentProvider.getByUser(42)).thenReturn(null);

    given()
        .contentType("application/json")
        .body("{\"user\": 42}")
        .when()
        .post("/api/comment/user")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter value: id"));

    verify(commentProvider, times(1)).getByUser(42);
  }
}
