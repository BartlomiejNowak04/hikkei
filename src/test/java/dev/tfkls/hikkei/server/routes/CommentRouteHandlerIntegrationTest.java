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
    CommentData comment1 = new CommentData(1, 10, 1, "First comment", null);
    CommentData comment2 = new CommentData(2, 10, 1, "Second comment", null);

    when(commentProvider.getByUser(42)).thenReturn(List.of(comment1, comment2));

    given()
        .when()
        .get("/api/comment/user/42")
        .then()
        .statusCode(200)
        .body("$", hasSize(2))
        .body("[0].id", equalTo(1))
        .body("[0].page_id", equalTo(10))
        .body("[0].user_id", equalTo(1))
        .body("[0].content", equalTo("First comment"))
        .body("[0].date", equalTo(null))
        .body("[1].id", equalTo(2))
        .body("[1].page_id", equalTo(10))
        .body("[1].user_id", equalTo(1))
        .body("[1].content", equalTo("Second comment"))
        .body("[1].date", equalTo(null));

    verify(commentProvider, times(1)).getByUser(42);
  }

  @Test
  void testGetByUserNullResponseReturns400() throws Exception {
    when(commentProvider.getByUser(42)).thenReturn(null);

    given()
        .when()
        .get("/api/comment/user/42")
        .then()
        .statusCode(400)
        .body("message", equalTo("Incorrect parameter value: id"));

    verify(commentProvider, times(1)).getByUser(42);
  }
}
