package dev.tfkls.hikkei.server.routes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Test;

public class ProviderErrorRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {

  @Test
  void internalErrorUserByidTest() throws Exception {
    when(userProvider.getUserData(1)).thenThrow(new InternalErrorException("test"));
    given()
        .body(new JsonObject().put("id", 1).toString())
        .when()
        .post("/api/user/byid")
        .then()
        .statusCode(500)
        .body("message", equalTo("test"));
  }
}
