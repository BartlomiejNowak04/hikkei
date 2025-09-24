package dev.tfkls.hikkei.server.util;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import io.restassured.http.Cookie;
import io.restassured.http.Method;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class RestCaller {
  public static ResponseBodyExtractionOptions callEndpoint(
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

  public static ResponseBodyExtractionOptions callEndpoint(
      String endpoint,
      Method method,
      JsonObject body,
      int statusCode,
      String contentType,
      Cookie cookie) {
    return given()
        .when()
        .body(body == null ? "" : body.toString())
        .contentType("application/json")
        .cookie(cookie)
        .request(method, endpoint)
        .then()
        .statusCode(statusCode)
        .contentType(contentType)
        .extract()
        .body();
  }

  public static JsonObject callEndpointObject(
      String endpoint, Method method, JsonObject body, int statusCode) {
    return new JsonObject(
        callEndpoint(endpoint, method, body, statusCode, "application/json").asString());
  }

  public static JsonObject callGetEndpointObject(String endpoint, int statusCode) {
    return callEndpointObject(endpoint, Method.GET, null, statusCode);
  }

  public static JsonArray callEndpointArray(
      String endpoint, Method method, JsonObject body, int statusCode) {
    return new JsonArray(
        callEndpoint(endpoint, method, body, statusCode, "application/json").asString());
  }

  public static JsonArray callGetEndpointArray(String endpoint, int statusCode) {
    return callEndpointArray(endpoint, Method.GET, null, statusCode);
  }

  public static void testException(String endpoint, Method method, JsonObject body, Exception e) {
    JsonObject bd =
        callEndpointObject(endpoint, method, body, e instanceof InternalErrorException ? 500 : 400);
    assertEquals(e.getMessage(), bd.getString("message"));
  }

  public static void testGetException(String endpoint, Exception e) {
    testException(endpoint, Method.GET, null, e);
  }
}
