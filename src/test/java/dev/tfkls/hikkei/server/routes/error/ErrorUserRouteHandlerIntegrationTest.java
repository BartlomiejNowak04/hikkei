package dev.tfkls.hikkei.server.routes.error;

import static dev.tfkls.hikkei.server.util.RestCaller.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.server.routes.RouteHandlerIntegrationTest;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErrorUserRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private InternalErrorException ie;
  private BadDataException bad;
  private JsonObject name;

  @BeforeAll
  void init() {
    ie = new InternalErrorException("testintr111");
    bad = new BadDataException("badtest2221");
    name = new JsonObject().put("name", "newname");
  }

  @Test
  void byIdInternalErrorTest() throws Exception {
    when(userProvider.getUserData(1)).thenThrow(ie);
    testGetException("/api/user/1", ie);
  }

  @Test
  void pfpInternalErrorUserTest() throws Exception {
    when(userProvider.getUserData(1)).thenThrow(ie);
    testGetException("/api/user/pfp/1", ie);
  }

  @Test
  void pfpInternalErrorFileTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(new UserData("test", new byte[0]));
    when(fileProvider.getContent(new byte[0])).thenThrow(ie);
    testGetException("/api/user/pfp/1", ie);
  }

  @Test
  void pfpBadDataTest() throws Exception {
    when(userProvider.getUserData(1)).thenReturn(new UserData("test", new byte[0]));
    when(fileProvider.getContent(new byte[0])).thenReturn(null);
    testGetException("/api/user/pfp/1", new InternalErrorException("File not found"));
  }

  @Test
  void categoriesInternalErrorTest() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenThrow(ie);
    testGetException("/api/user/categories/1", ie);
  }

  @Test
  void categoriesInternalErrorTest2() throws Exception {
    when(userProvider.getCategoriesPermissions(1)).thenReturn(Map.of(1, PermissionLevel.ADMIN));
    when(categoryProvider.getById(1)).thenThrow(ie);
    testGetException("/api/user/categories/1", ie);
  }

  @Test
  void createBadDataTest() throws Exception {
    when(userProvider.createUser(any())).thenThrow(bad);
    testException("/api/user/create", Method.POST, name, bad);
  }

  @Test
  void createInternalErrorTest() throws Exception {
    when(userProvider.createUser(any())).thenThrow(ie);
    testException("/api/user/create", Method.POST, name, ie);
  }

  // TODO: mock session and uncomment
  //  @Test
  //  void updateNameBadDataTest() throws Exception {
  //    doThrow(bad).when(userProvider).updateUsername(1, "newname");
  //    testException("/api/user/update/name", Method.PUT, name1, bad);
  //  }
  //
  //  @Test
  //  void updateNameInternalErrorTest() throws Exception {
  //    doThrow(ie).when(userProvider).updateUsername(1, "newname");
  //    testException("/api/user/update/name", Method.PUT, name1, ie);
  //  }
  //
  //  @Test
  //  void deleteInternalErrorTest() throws Exception {
  //    doThrow(ie).when(userProvider).deleteUser(1);
  //    testException("/api/user/delete", Method.DELETE, id1, ie);
  //  }
  //
  //  @Test
  //  void updatePfpBadDataTest() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenReturn(true);
  //    doThrow(new BadDataException("test")).when(userProvider).updateUserPicture(anyInt(), any());
  //    given()
  //        .multiPart("id", 1)
  //        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}),
  // "image/png")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(400)
  //        .body("message", equalTo("test"));
  //  }
  //
  //  @Test
  //  void updatePfpInternalErrorTest() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenThrow(new InternalErrorException("test"));
  //    given()
  //        .multiPart("id", 1)
  //        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}),
  // "image/png")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(500)
  //        .body("message", equalTo("test"));
  //  }
  //
  //  @Test
  //  void updatePfpInternalErrorTest2() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenReturn(true);
  //    doThrow(new InternalErrorException("test"))
  //        .when(userProvider)
  //        .updateUserPicture(anyInt(), any());
  //    given()
  //        .multiPart("id", 1)
  //        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}),
  // "image/png")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(500)
  //        .body("message", equalTo("test"));
  //  }
  //
  //  @Test
  //  void updatePfpInternalErrorTest3() throws Exception {
  //    when(userProvider.doesUserExist(1)).thenReturn(true);
  //    doThrow(new InternalErrorException("test")).when(fileProvider).storeContent(any(), any());
  //    given()
  //        .multiPart("id", 1)
  //        .multiPart("file", "mock.png", new ByteArrayInputStream(new byte[] {1, 2, 3}),
  // "image/png")
  //        .when()
  //        .put("/api/user/update/pfp")
  //        .then()
  //        .statusCode(500)
  //        .body("message", equalTo("test"));
  //  }
}
