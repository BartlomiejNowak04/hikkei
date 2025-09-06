package dev.tfkls.hikkei.server.routes;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verifyNoInteractions;

import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ParametersRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private void callApi(String endpoint, Method method, String body, String message) {
    given()
        .when()
        .body(body)
        .request(method, endpoint)
        .then()
        .statusCode(400)
        .body("message", equalTo(message));

    verifyNoInteractions(userProvider);
    verifyNoInteractions(pageProvider);
    verifyNoInteractions(revisionProvider);
    verifyNoInteractions(commentProvider);
    verifyNoInteractions(categoryProvider);
    verifyNoInteractions(versionProvider);
    verifyNoInteractions(fileProvider);
  }

  private void assertEndpoint(String endpoint, Method method, Map<String, Object> constraints) {
    String[] args = constraints.keySet().toArray(new String[0]);

    for (String current : args) {
      JsonObject json = new JsonObject();
      for (String arg : args) if (!arg.equals(current)) json.put(arg, constraints.get(arg));

      Object value;
      if (constraints.get(current) instanceof String) value = 0;
      else value = "invalid";

      callApi(
          endpoint, method, json.put(current, value).toString(), "Incorrect parameter: " + current);
      callApi(
          endpoint, method, json.putNull(current).toString(), "Incorrect parameter: " + current);
      json.remove(current);
      callApi(endpoint, method, json.toString(), "Missing required parameter: " + current);
    }

    callApi(endpoint, method, "", "No data provided");
  }

  // TODO: uncomment when implemented
  // /api/user
  //  @Test
  //  public void apiUserLoginRejectsImproperData() {
  //    assertEndpoint("/api/user/login", Method.POST, Map.of());
  //  }
  //
  @Test
  public void apiUserByidRejectsImproperData() {
    assertEndpoint("/api/user/byid", Method.POST, Map.of("id", 0));
  }

  @Test
  public void apiUserPfpRejectsImproperData() {
    assertEndpoint("/api/user/pfp", Method.POST, Map.of("id", 0));
  }

  @Test
  public void apiUserCreateRejectsImproperData() {
    assertEndpoint("/api/user/create", Method.PUT, Map.of("name", "example"));
  }

  @Test
  public void apiUserUpdateNameRejectsImproperData() {
    assertEndpoint("/api/user/update/name", Method.PUT, Map.of("id", 0, "name", "newName"));
  }

  @Test
  public void apiUserDeleteRejectsImproperData() {
    assertEndpoint("/api/user/delete", Method.DELETE, Map.of("id", 0));
  }

  // /api/page/
  @Test
  public void apiPageBytitleRejectsImproperData() {
    assertEndpoint("/api/page/bytitle", Method.POST, Map.of("title", "Example Title"));
  }

  @Test
  public void apiPageByurlRejectsImproperData() {
    assertEndpoint("/api/page/byurl", Method.POST, Map.of("url", "http://example.com"));
  }

  @Test
  public void apiPageSearchRejectsImproperData() {
    assertEndpoint("/api/page/search", Method.POST, Map.of("title", "search phrase"));
  }

  //  @Test
  //  public void apiPageCategoryRejectsImproperData() {
  //    assertEndpoint("/api/page/category", Method.POST, Map.of("category", 0));
  //  }

  @Test
  public void apiPageCreateRejectsImproperData() {
    assertEndpoint(
        "/api/page/create", Method.PUT, Map.of("title", "New Page", "category", 0, "user", 0));
  }

  @Test
  public void apiPageUpdateTitleRejectsImproperData() {
    assertEndpoint("/api/page/update/title", Method.PUT, Map.of("id", 0, "title", "Updated Title"));
  }

  @Test
  public void apiPageUpdateCategoryRejectsImproperData() {
    assertEndpoint("/api/page/update/category", Method.PUT, Map.of("id", 0, "category", 0));
  }

  @Test
  public void apiPageUpdateArchiveRejectsImproperData() {
    assertEndpoint("/api/page/update/archive", Method.PUT, Map.of("id", 0, "value", true));
  }

  // /api/revision/
  @Test
  public void apiRevisionUserRejectsImproperData() {
    assertEndpoint("/api/revision/user", Method.POST, Map.of("user", 0));
  }

  @Test
  public void apiRevisionPageRejectsImproperData() {
    assertEndpoint("/api/revision/page", Method.POST, Map.of("page", 0));
  }

  //  @Test
  //  public void apiRevisionRenderRejectsImproperData() {
  //    assertEndpoint("/api/revision/render", Method.POST, Map.of("content", "Sample content"));
  //  }

  @Test
  public void apiRevisionContentRejectsImproperData() {
    assertEndpoint("/api/revision/content", Method.POST, Map.of("id", 0));
  }

  @Test
  public void apiRevisionByidRejectsImproperData() {
    assertEndpoint("/api/revision/byid", Method.POST, Map.of("id", 0));
  }

  @Test
  public void apiRevisionDraftRejectsImproperData() {
    assertEndpoint("/api/revision/draft", Method.POST, Map.of("page", 0, "user", 0));
  }

  @Test
  public void apiRevisionCreateRejectsImproperData() {
    assertEndpoint(
        "/api/revision/create",
        Method.PUT,
        Map.of("id", 0, "content", "New revision content", "user", 0));
  }

  @Test
  public void apiRevisionUpdateStatusRejectsImproperData() {
    assertEndpoint("/api/revision/update/status", Method.PUT, Map.of("id", 0, "status", true));
  }

  @Test
  public void apiRevisionDraftSaveRejectsImproperData() {
    assertEndpoint(
        "/api/revision/draft/save",
        Method.PUT,
        Map.of("page", 0, "user", 0, "content", "Draft content"));
  }

  @Test
  public void apiRevisionDraftDeleteRejectsImproperData() {
    assertEndpoint("/api/revision/draft/delete", Method.DELETE, Map.of("page", 0, "user", 0));
  }

  @Test
  public void apiRevisionDeleteRejectsImproperData() {
    assertEndpoint("/api/revision/delete", Method.DELETE, Map.of("id", 0));
  }

  // /api/comment/
  @Test
  public void apiCommentUserRejectsImproperData() {
    assertEndpoint("/api/comment/user", Method.POST, Map.of("user", 0));
  }

  @Test
  public void apiCommentPageRejectsImproperData() {
    assertEndpoint("/api/comment/page", Method.POST, Map.of("page", 0));
  }

  @Test
  public void apiCommentByidRejectsImproperData() {
    assertEndpoint("/api/comment/byid", Method.POST, Map.of("id", 0));
  }

  @Test
  public void apiCommentCreateRejectsImproperData() {
    assertEndpoint(
        "/api/comment/create", Method.PUT, Map.of("id", 0, "content", "Comment text", "user", 0));
  }

  @Test
  public void apiCommentDeleteRejectsImproperData() {
    assertEndpoint("/api/comment/delete", Method.DELETE, Map.of("id", 0));
  }

  // /api/category/
  //  @Test
  //  public void apiCategoryAllRejectsImproperData() {
  //    assertEndpoint("/api/category/", Method.POST, Map.of());
  //  }
  //
  //  @Test
  //  public void apiCategoryByidRejectsImproperData() {
  //    assertEndpoint("/api/category/byid", Method.POST, Map.of("id", 0));
  //  }
  //
  //  @Test
  //  public void apiCategoryPermissionRejectsImproperData() {
  //    assertEndpoint("/api/category/permission", Method.POST, Map.of("category", 0, "user", 0,
  // "permission", 0));
  //  }
  //
  //  @Test
  //  public void apiCategoryCreateRejectsImproperData() {
  //    assertEndpoint("/api/category/create", Method.PUT, Map.of("name", "New Category"));
  //  }
  //
  //  @Test
  //  public void apiCategoryUpdateNameRejectsImproperData() {
  //    assertEndpoint(
  //        "/api/category/update/name", Method.PUT, Map.of("id", 0, "name", "Updated Category"));
  //  }
}
