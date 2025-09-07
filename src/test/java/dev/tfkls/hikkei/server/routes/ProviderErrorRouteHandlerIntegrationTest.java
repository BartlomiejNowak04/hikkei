package dev.tfkls.hikkei.server.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ProviderErrorRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private InternalErrorException ie;
  private BadDataException bad;
  private JsonObject id1;
  private JsonObject user1;
  private JsonObject name;
  private JsonObject name1;

  @BeforeAll
  void init() {
    ie = new InternalErrorException("testintr111");
    bad = new BadDataException("badtest2221");
    id1 = new JsonObject().put("id", 1);
    user1 = new JsonObject().put("user", 1);
    name = new JsonObject().put("name", "newname");
    name1 = new JsonObject().put("name", "newname").put("id", 1);
  }

  private void testException(String endpoint, Method method, JsonObject body, Exception e) {
    JsonObject bd =
        callEndpointObject(endpoint, method, body, e instanceof InternalErrorException ? 500 : 400);
    assertEquals(e.getMessage(), bd.getString("message"));
  }

  @Test
  void byIdInternalErrorTest() throws Exception {
    when(userProvider.getUserData(1)).thenThrow(ie);
    testException("/api/user/byid", Method.POST, id1, ie);
  }

  @Test
  void pfpInternalErrorUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenThrow(ie);
    testException("/api/user/pfp", Method.POST, id1, ie);
  }

  @Test
  void pfpInternalErrorFileTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(new UserData("test", new byte[0]));
    when(fileProvider.getContent(new byte[0])).thenThrow(ie);
    testException("/api/user/pfp", Method.POST, id1, ie);
  }

  @Test
  void pfpBadDataTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(new UserData("test", new byte[0]));
    when(fileProvider.getContent(new byte[0])).thenThrow(bad);
    testException("/api/user/pfp", Method.POST, id1, bad);
  }

  @Test
  void categoriesInternalErrorTest() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenThrow(ie);
    testException("/api/user/categories", Method.POST, user1, ie);
  }

  @Test
  void categoriesInternalErrorTest2() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenReturn(Map.of(1, PermissionLevel.Admin));
    when(categoryProvider.getById(1)).thenThrow(ie);
    testException("/api/user/categories", Method.POST, user1, ie);
  }

  @Test
  void createBadDataTest() throws Exception {
    when(userProvider.createUser(any())).thenThrow(bad);
    testException("/api/user/create", Method.PUT, name, bad);
  }

  @Test
  void createInternalErrorTest() throws Exception {
    when(userProvider.createUser(any())).thenThrow(ie);
    testException("/api/user/create", Method.PUT, name, ie);
  }

  @Test
  void updateNameBadDataTest() throws Exception {
    doThrow(bad).when(userProvider).updateUsername(1, "newname");
    testException("/api/user/update/name", Method.PUT, name1, bad);
  }

  @Test
  void updateNameInternalErrorTest() throws Exception {
    doThrow(ie).when(userProvider).updateUsername(1, "newname");
    testException("/api/user/update/name", Method.PUT, name1, ie);
  }

  @Test
  void deleteInternalErrorTest() throws Exception {
    doThrow(ie).when(userProvider).deleteUser(1);
    testException("/api/user/delete", Method.DELETE, id1, ie);
  }
}
