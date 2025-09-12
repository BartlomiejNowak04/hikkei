package dev.tfkls.hikkei.server.routes.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.server.routes.RouteHandlerIntegrationTest;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErrorCategoryRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private InternalErrorException ie;
  private BadDataException bad;
  private JsonObject perm;
  private JsonObject name;

  @BeforeAll
  void init() {
    ie = new InternalErrorException("categorydkjdfie");
    bad = new BadDataException("categorydsdfkbad");
    perm = new JsonObject().put("category", 1).put("user", 1).put("permission", "NoAccess");
    name = new JsonObject().put("name", "dsdg");
  }

  @Test
  void getAllInternalErrorTest() throws Exception {
    when(categoryProvider.getCategories()).thenThrow(ie);
    testGetException("/api/category/all", ie);
  }

  @Test
  void byIdInternalErrorTest() throws Exception {
    when(categoryProvider.getById(1)).thenThrow(ie);
    testGetException("/api/category/1", ie);
  }

  @Test
  void usersInternalErrorTest() throws Exception {
    when(categoryProvider.getUsersPermissions(anyInt())).thenThrow(ie);
    testGetException("/api/category/users/1", ie);
  }

  @Test
  void usersInternalErrorTest2() throws Exception {
    when(categoryProvider.getUsersPermissions(anyInt()))
        .thenReturn(Map.of(1, PermissionLevel.Admin));
    when(userProvider.getUserData(1)).thenThrow(ie);
    testGetException("/api/category/users/1", ie);
  }

  @Test
  void permissionInternalErrorException() throws Exception {
    doThrow(ie).when(categoryProvider).updatePermission(anyInt(), anyInt(), any());
    testException("/api/category/permission", Method.PUT, perm, ie);
  }

  @Test
  void permissionBadDataException() throws Exception {
    doThrow(bad).when(categoryProvider).updatePermission(anyInt(), anyInt(), any());
    testException("/api/category/permission", Method.PUT, perm, bad);
  }

  @Test
  void createInternalErrorException() throws Exception {
    doThrow(ie).when(categoryProvider).createCategory(any());
    testException("/api/category/create", Method.POST, name, ie);
  }

  @Test
  void createBadDataException() throws Exception {
    doThrow(bad).when(categoryProvider).createCategory(any());
    testException("/api/category/create", Method.POST, name, bad);
  }

  @Test
  void updateNameInternalErrorException() throws Exception {
    doThrow(ie).when(categoryProvider).changeName(anyInt(), any());
    testException("/api/category/update/name", Method.PUT, name.put("id", 1), ie);
  }

  @Test
  void updateNameBadDataException() throws Exception {
    doThrow(bad).when(categoryProvider).changeName(anyInt(), any());
    testException("/api/category/update/name", Method.PUT, name.put("id", 1), bad);
  }

  @Test
  void deleteInternalErrorException() throws Exception {
    doThrow(ie).when(categoryProvider).deleteCategory(anyInt());
    testException("/api/category/delete", Method.DELETE, new JsonObject().put("id", 1), ie);
  }
}
