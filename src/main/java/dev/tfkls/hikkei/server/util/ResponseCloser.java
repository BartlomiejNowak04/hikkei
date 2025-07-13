package dev.tfkls.hikkei.server.util;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;

public class ResponseCloser {
  public static void throwError(int statusCode, String message, HttpServerResponse response) {
    response.setStatusCode(statusCode);
    response.end(new JsonObject().put("message", message).encode());
  }

  public static void endWithStatus(int statusCode, HttpServerResponse response) {
    response.setStatusCode(statusCode);
    response.end();
  }
}
