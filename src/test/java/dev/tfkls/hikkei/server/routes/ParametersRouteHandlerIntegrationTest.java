package dev.tfkls.hikkei.server.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;

import dev.tfkls.hikkei.server.util.Type;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class ParametersRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private void callApi(String endpoint, Method method, JsonObject body, String message) {
    JsonObject res = callEndpointObject(endpoint, method, body, 400);
    assertEquals(message, res.getString("message"));

    verifyNoInteractions(userProvider);
    verifyNoInteractions(pageProvider);
    verifyNoInteractions(revisionProvider);
    verifyNoInteractions(commentProvider);
    verifyNoInteractions(categoryProvider);
    verifyNoInteractions(versionProvider);
    verifyNoInteractions(fileProvider);
  }

  private Object goodData(Type type) {
    return switch (type) {
      case Int -> 111;
      case Str -> "jdkjjwhe";
      case Bool -> true;
      case Binary -> new JsonObject().put("tmp", new byte[] {1, 2, 3}).getString("tmp");
    };
  }

  private Object badData(Type type) {
    return switch (type) {
      case Int -> "dsdsf";
      case Str -> 11;
      case Bool -> "tthkk";
      case Binary -> "aa";
    };
  }

  private void assertEndpoint(
      String endpoint,
      Method method,
      Map<String, Type> constraints,
      List<Type> pathConstraints,
      List<String> name) {
    String[] args = constraints.keySet().toArray(new String[0]);
    JsonObject goodJson = new JsonObject();
    for (String arg : args) goodJson.put(arg, goodData(constraints.get(arg)));

    StringBuilder goodPathBuilder = new StringBuilder();
    for (Type path : pathConstraints) goodPathBuilder.append("/").append(goodData(path));
    String goodPath = goodPathBuilder.toString();

    for (int i = 0; i < pathConstraints.size(); i++) {
      Type type = pathConstraints.get(i);
      if (type == Type.Str) continue;
      StringBuilder path = new StringBuilder();
      for (int j = 0; j < pathConstraints.size(); j++)
        path.append("/").append(i == j ? badData(type) : goodData(type));
      String p = path.toString();

      callApi(endpoint + p, method, goodJson, "Incorrect parameter: " + name.get(i));
    }

    for (String current : args) {
      JsonObject json = new JsonObject();
      for (String arg : args)
        if (!arg.equals(current)) json.put(arg, goodData(constraints.get(arg)));

      if (constraints.get(current) != Type.Str)
        callApi(
            endpoint + goodPath,
            method,
            json.put(current, badData(constraints.get(current))),
            "Incorrect parameter: " + current);
      callApi(
          endpoint + goodPath, method, json.putNull(current), "Incorrect parameter: " + current);
      json.remove(current);
      callApi(endpoint + goodPath, method, json, "Missing required parameter: " + current);
    }

    if (!constraints.isEmpty()) callApi(endpoint, method, null, "No data provided");
  }

  private void assertGetEndpoint(String endpoint, List<Type> pathConstraints, List<String> name) {
    assertEndpoint(endpoint, Method.GET, Map.of(), pathConstraints, name);
  }

  private void assertJsonEndpoint(String endpoint, Method method, Map<String, Type> constraints) {
    assertEndpoint(endpoint, method, constraints, List.of(), List.of());
  }

  // TODO: uncomment when implemented
  // /api/user
  @Test
  public void apiUserByidRejectsImproperData() {
    assertGetEndpoint("/api/user", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiUserPfpRejectsImproperData() {
    assertGetEndpoint("/api/user/pfp", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiUserCreateRejectsImproperData() {
    assertJsonEndpoint("/api/user/create", Method.POST, Map.of("name", Type.Str));
  }

  @Test
  public void apiUserCategoriesRejectsImproperData() {
    assertGetEndpoint("/api/user/categories", List.of(Type.Int), List.of("id"));
  }

  //  @Test
  //  public void apiUserUpdateNameRejectsImproperData() {
  //    assertEndpoint("/api/user/update/name", Method.PUT, Map.of("id", 0, "name", "newName"));
  //  }
  //
  //  @Test
  //  public void apiUserDeleteRejectsImproperData() {
  //    assertEndpoint("/api/user/delete", Method.DELETE, Map.of("id", 0));
  //  }

  // /api/page/
  @Test
  public void apiPageBytitleRejectsImproperData() {
    assertGetEndpoint("/api/page/title", List.of(Type.Str), List.of("title"));
  }

  @Test
  public void apiPageByurlRejectsImproperData() {
    assertGetEndpoint("/api/page", List.of(Type.Str), List.of("url"));
  }

  @Test
  public void apiPageSearchRejectsImproperData() {
    assertGetEndpoint("/api/page/search", List.of(Type.Str), List.of("title"));
  }

  @Test
  public void apiPageCategoryRejectsImproperData() {
    assertGetEndpoint("/api/page/category", List.of(Type.Int), List.of("id"));
  }

  //  @Test
  //  public void apiPageCreateRejectsImproperData() {
  //    assertJsonEndpoint(
  //        "/api/page/create", Method.POST, Map.of("title", Type.Str, "category", Type.Int));
  //  }

  @Test
  public void apiPageUpdateTitleRejectsImproperData() {
    assertJsonEndpoint(
        "/api/page/update/title", Method.PUT, Map.of("id", Type.Int, "title", Type.Str));
  }

  @Test
  public void apiPageUpdateCategoryRejectsImproperData() {
    assertJsonEndpoint(
        "/api/page/update/category", Method.PUT, Map.of("id", Type.Int, "category", Type.Int));
  }

  @Test
  public void apiPageUpdateArchiveRejectsImproperData() {
    assertJsonEndpoint(
        "/api/page/update/archive", Method.PUT, Map.of("id", Type.Int, "value", Type.Bool));
  }

  @Test
  public void apiPageDeleteRejectsImproperData() {
    assertJsonEndpoint("/api/page/delete", Method.DELETE, Map.of("id", Type.Int));
  }

  // /api/revision/
  @Test
  public void apiRevisionUserRejectsImproperData() {
    assertGetEndpoint("/api/revision/user", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiRevisionPageRejectsImproperData() {
    assertGetEndpoint("/api/revision/page", List.of(Type.Int), List.of("id"));
  }

  //  @Test
  //  public void apiRevisionRenderRejectsImproperData() {
  //    assertEndpoint("/api/revision/render", Method.POST, Map.of("content", "Sample content"));
  //  }

  @Test
  public void apiRevisionContentRejectsImproperData() {
    assertGetEndpoint("/api/revision/content", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiRevisionByidRejectsImproperData() {
    assertGetEndpoint("/api/revision", List.of(Type.Int), List.of("id"));
  }

  //  @Test
  //  public void apiRevisionDraftRejectsImproperData() {
  //    assertGetEndpoint("/api/revision/draft", List.of(Type.Int), List.of("page_id"));
  //  }

  //  @Test
  //  public void apiRevisionCreateRejectsImproperData() {
  //    assertJsonEndpoint(
  //        "/api/revision/create",
  //        Method.POST,
  //        Map.of("page_id", Type.Int, "content", Type.Str));
  //  }

  @Test
  public void apiRevisionUpdateStatusRejectsImproperData() {
    assertJsonEndpoint(
        "/api/revision/update/status", Method.PUT, Map.of("id", Type.Int, "status", Type.Bool));
  }

  //  @Test
  //  public void apiRevisionDraftSaveRejectsImproperData() {
  //    assertJsonEndpoint(
  //        "/api/revision/draft/save",
  //        Method.POST,
  //        Map.of("page", Type.Int, "content", Type.Str));
  //  }

  //  @Test
  //  public void apiRevisionDraftDeleteRejectsImproperData() {
  //    assertJsonEndpoint("/api/revision/draft/delete", Method.DELETE, Map.of("page", Type.Int));
  //  }

  @Test
  public void apiRevisionDeleteRejectsImproperData() {
    assertJsonEndpoint("/api/revision/delete", Method.DELETE, Map.of("id", Type.Int));
  }

  // /api/comment/
  @Test
  public void apiCommentUserRejectsImproperData() {
    assertGetEndpoint("/api/comment/user", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiCommentPageRejectsImproperData() {
    assertGetEndpoint("/api/comment/page", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiCommentByidRejectsImproperData() {
    assertGetEndpoint("/api/comment", List.of(Type.Int), List.of("id"));
  }

  //  @Test
  //  public void apiCommentCreateRejectsImproperData() {
  //    assertJsonEndpoint(
  //        "/api/comment/create", Method.POST, Map.of("page_id", Type.Int, "content", Type.Str));
  //  }

  @Test
  public void apiCommentDeleteRejectsImproperData() {
    assertJsonEndpoint("/api/comment/delete", Method.DELETE, Map.of("id", Type.Int));
  }

  // /api/category/
  @Test
  public void apiCategoryByidRejectsImproperData() {
    assertGetEndpoint("/api/category", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiCategoryUsersRejectsImproperData() {
    assertGetEndpoint("/api/category/users", List.of(Type.Int), List.of("id"));
  }

  @Test
  public void apiCategoryPermissionRejectsImproperData() {
    assertJsonEndpoint(
        "/api/category/permission",
        Method.PUT,
        Map.of("category", Type.Int, "user", Type.Int, "permission", Type.Str));
  }

  @Test
  public void apiCategoryCreateRejectsImproperData() {
    assertJsonEndpoint("/api/category/create", Method.POST, Map.of("name", Type.Str));
  }

  @Test
  public void apiCategoryUpdateNameRejectsImproperData() {
    assertJsonEndpoint(
        "/api/category/update/name", Method.PUT, Map.of("id", Type.Int, "name", Type.Str));
  }
}
