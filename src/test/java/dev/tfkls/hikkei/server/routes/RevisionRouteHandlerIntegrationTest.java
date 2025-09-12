package dev.tfkls.hikkei.server.routes;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import io.restassured.http.Method;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RevisionRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private RevisionData rd;

  @BeforeAll
  void init() {
    rd = new RevisionData(1, 1, 1, new byte[] {1, 2}, LocalDateTime.now(), true);
  }

  private void assertRevision(RevisionData rd, JsonObject body) {
    assertEquals(rd.id(), body.getInteger("id"));
    assertEquals(rd.userId(), body.getInteger("user_id"));
    assertEquals(rd.pageId(), body.getInteger("page_id"));
    assertArrayEquals(rd.file(), body.getBinary("filename"));
    assertEquals(rd.date().toString(), body.getString("date"));
    assertEquals(rd.approved(), body.getBoolean("approved"));
  }

  @Test
  void userNoDataTest() throws Exception {
    when(revisionProvider.getByUser(1)).thenReturn(null);
    testGetException("/api/revision/user/1", new BadDataException("Incorrect parameter value: id"));
  }

  @Test
  void userEmptyTest() throws Exception {
    when(revisionProvider.getByUser(1)).thenReturn(List.of());
    JsonArray body = callGetEndpointArray("/api/revision/user/1", 200);
    assertEquals(0, body.size());
  }

  @Test
  void userWorkingTest() throws Exception {
    when(revisionProvider.getByUser(1)).thenReturn(List.of(rd));
    JsonArray body = callGetEndpointArray("/api/revision/user/1", 200);
    assertEquals(1, body.size());
    assertRevision(rd, body.getJsonObject(0));
  }

  @Test
  void pageNoDataTest() throws Exception {
    when(revisionProvider.getByPage(1)).thenReturn(null);
    testGetException("/api/revision/page/1", new BadDataException("Incorrect parameter value: id"));
  }

  @Test
  void pageEmptyTest() throws Exception {
    when(revisionProvider.getByPage(1)).thenReturn(List.of());
    JsonArray body = callGetEndpointArray("/api/revision/page/1", 200);
    assertEquals(0, body.size());
  }

  @Test
  void pageWorkingTest() throws Exception {
    when(revisionProvider.getByPage(1)).thenReturn(List.of(rd));
    JsonArray body = callGetEndpointArray("/api/revision/page/1", 200);
    assertEquals(1, body.size());
    assertRevision(rd, body.getJsonObject(0));
  }

  @Test
  void contentNoPageTest() throws Exception {
    when(revisionProvider.getByPage(1)).thenReturn(null);
    testGetException("/api/revision/content/1", new BadDataException("Invalid page id"));
  }

  @Test
  void contentEmptyTest() throws Exception {
    when(revisionProvider.getByPage(1)).thenReturn(new ArrayList<>());
    JsonObject body = callGetEndpointObject("/api/revision/content/1", 200);
    assertEquals("", body.getString("content"));
  }

  @Test
  void currentWorkingTest() throws Exception {
    when(revisionProvider.getByPage(1)).thenReturn(new ArrayList<>(Collections.singleton(rd)));
    when(versionProvider.getVersion(any())).thenReturn("test");
    JsonObject body = callGetEndpointObject("/api/revision/content/1", 200);
    assertEquals("test", body.getString("content"));
  }

  // TODO: test the rendering
  @Test
  void currentRenderTest() throws Exception {}

  @Test
  void byIdNoDataTest() throws Exception {
    when(revisionProvider.getById(1)).thenReturn(null);
    testGetException("/api/revision/1", new BadDataException("Incorrect parameter value: id"));
  }

  @Test
  void byIdDeletedTest() throws Exception {
    when(revisionProvider.getById(1)).thenReturn(RevisionData.DELETED);
    callEndpoint("/api/revision/1", Method.GET, null, 404, "");
  }

  @Test
  void byIdWorkingTest() throws Exception {
    when(revisionProvider.getById(1)).thenReturn(rd);
    JsonObject body = callGetEndpointObject("/api/revision/1", 200);
    assertRevision(rd, body);
  }

  //  @Test
  //  void draftNotSaved() throws Exception {
  //    when(revisionProvider.getDraft(1, 1)).thenReturn(null);
  //    JsonObject body = callGetEndpointObject("/api/revision/draft/1", 200);
  //    assertEquals("", body.getString("content"));
  //  }

  //  @Test
  //  void draftWorkingTest() throws Exception {
  //    when(revisionProvider.getDraft(1, 1)).thenReturn(new byte[0]);
  //    when(versionProvider.getVersion(any())).thenReturn("test");
  //    JsonObject body = callGetEndpointObject("/api/revision/draft/1", 200);
  //    assertEquals("test", body.getString("content"));
  //  }

  //  @Test
  //  void createWorkingTest() throws Exception {
  //    when(versionProvider.storeVersion(any(), any())).thenReturn(new byte[0]);
  //    when(revisionProvider.createRevision(any())).thenReturn(7);
  //    JsonObject body =
  //        callEndpointObject(
  //            "/api/revision/create",
  //            Method.POST,
  //            new JsonObject().put("page_id", 1).put("content", "test"),
  //            201);
  //    assertEquals(7, body.getInteger("id"));
  //  }

  @Test
  void updateStatusWorkingTest() {
    callEndpoint(
        "/api/revision/update/status",
        Method.PUT,
        new JsonObject().put("id", 1).put("status", true),
        204,
        "");
  }

  //  @Test
  //  void draftSaveWorkingTest() {
  //    callEndpoint(
  //        "/api/revision/draft/save",
  //        Method.POST,
  //        new JsonObject().put("page", 1).put("content", "aa"),
  //        204,
  //        "");
  //  }

  //  @Test
  //  void draftDeleteWorkingTest() {
  //    callEndpoint("/api/revision/draft/delete", Method.DELETE, new JsonObject().put("page", 1),
  // 204, "");
  //  }

  @Test
  void deleteWorkingTest() {
    callEndpoint("/api/revision/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
  }
}
