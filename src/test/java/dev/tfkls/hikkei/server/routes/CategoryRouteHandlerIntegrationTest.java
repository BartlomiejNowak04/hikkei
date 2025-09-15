package dev.tfkls.hikkei.server.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import io.restassured.http.Method;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CategoryRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private CategoryData cd;

  @BeforeAll
  void init() {
    cd = new CategoryData(1, "nametest", LocalDateTime.now());
  }

  private void assertCategory(JsonObject body, CategoryData category) {
    assertEquals(category.id(), body.getInteger("id"));
    assertEquals(category.name(), body.getString("name"));
    assertEquals(category.created().toString(), body.getString("created"));
  }

  @Test
  void getAllWorkingTest() throws Exception {
    when(categoryProvider.getCategories()).thenReturn(List.of(cd));
    JsonArray body = callGetEndpointArray("/api/category/all", 200);
    assertEquals(1, body.size());
    assertCategory(body.getJsonObject(0), cd);
  }

  @Test
  void byIdNoCategoryTest() throws Exception {
    when(categoryProvider.getById(1)).thenReturn(null);
    testGetException("/api/category/1", new BadDataException("Invalid parameter: id"));
  }

  @Test
  void byIdWorkingTest() throws Exception {
    when(categoryProvider.getById(1)).thenReturn(cd);
    JsonObject body = callGetEndpointObject("/api/category/1", 200);
    assertCategory(body, cd);
  }

  @Test
  void usersNoCategoryTest() throws Exception {
    when(categoryProvider.getUsersPermissions(1)).thenReturn(null);
    testGetException("/api/category/users/1", new BadDataException("Invalid parameter: id"));
  }

  @Test
  void usersWorkingTest() throws Exception {
    when(categoryProvider.getUsersPermissions(1)).thenReturn(Map.of(1, PermissionLevel.ADMIN));
    when(userProvider.getUserData(1)).thenReturn(new UserData("aaa"));
    JsonArray body = callGetEndpointArray("/api/category/users/1", 200);
    assertEquals(1, body.size());
    assertEquals("aaa", body.getJsonObject(0).getString("username"));
    assertEquals(PermissionLevel.ADMIN.toString(), body.getJsonObject(0).getString("permission"));
  }

  @Test
  void usersNoDeletedTest() throws Exception {
    when(categoryProvider.getUsersPermissions(1)).thenReturn(Map.of(1, PermissionLevel.ADMIN));
    when(userProvider.getUserData(1)).thenReturn(UserData.DELETED);
    JsonArray body = callGetEndpointArray("/api/category/users/1", 200);
    assertEquals(0, body.size());
  }

  @Test
  void permissionInvalidPermissionTest() {
    testException(
        "/api/category/permission",
        Method.PUT,
        new JsonObject().put("category", 1).put("user", 1).put("permission", "ddfdf"),
        new BadDataException("Invalid permission type"));
  }

  @Test
  void permissionWorkingNullTest() {
    callEndpoint(
        "/api/category/permission",
        Method.PUT,
        new JsonObject().put("category", 1).put("user", 1).put("permission", "null"),
        204,
        "");
  }

  @Test
  void permissionWorkingTest() {
    callEndpoint(
        "/api/category/permission",
        Method.PUT,
        new JsonObject().put("category", 1).put("user", 1).put("permission", "NO_ACCESS"),
        204,
        "");
  }

  @Test
  void createWorkingTest() throws Exception {
    when(categoryProvider.createCategory(any())).thenReturn(1);
    JsonObject body =
        callEndpointObject(
            "/api/category/create", Method.POST, new JsonObject().put("name", "gfd"), 201);
    assertEquals(1, body.getInteger("id"));
  }

  @Test
  void updateNameWorkingTest() {
    callEndpoint(
        "/api/category/update/name",
        Method.PUT,
        new JsonObject().put("name", "aa").put("id", 1),
        204,
        "");
  }

  @Test
  void deleteWorkingTest() {
    callEndpoint("/api/category/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
  }
}
