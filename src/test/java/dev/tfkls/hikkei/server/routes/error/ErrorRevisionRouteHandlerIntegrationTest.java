package dev.tfkls.hikkei.server.routes.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.server.routes.RouteHandlerIntegrationTest;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErrorRevisionRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private InternalErrorException ie;
  private BadDataException bad;
  private JsonObject id1;
  private JsonObject user1;
  private JsonObject page1;
  private JsonObject draft1;
  private JsonObject create1;
  private JsonObject status1;
  private JsonObject draftSave1;

  @BeforeAll
  void init() {
    ie = new InternalErrorException("testrevIE");
    bad = new BadDataException("badrevBAD");
    id1 = new JsonObject().put("id", 1);
    user1 = new JsonObject().put("user", 5);
    page1 = new JsonObject().put("page", 7);
    draft1 = new JsonObject().put("page", 7).put("user", 5);
    create1 = new JsonObject().put("id", 7).put("content", "revision content").put("user", 5);
    status1 = new JsonObject().put("id", 1).put("status", true);
    draftSave1 = new JsonObject().put("page", 7).put("user", 5).put("content", "draft content");
  }

  @Test
  void userInternalErrorTest() throws Exception {
    when(revisionProvider.getByUser(5)).thenThrow(ie);
    testException("/api/revision/user", Method.POST, user1, ie);
  }

  @Test
  void pageInternalErrorTest() throws Exception {
    when(revisionProvider.getByPage(7)).thenThrow(ie);
    testException("/api/revision/page", Method.POST, page1, ie);
  }

  @Test
  void contentInternalErrorTest() throws Exception {
    when(revisionProvider.getByPage(1)).thenThrow(ie);
    testException("/api/revision/content", Method.POST, id1, ie);
  }

  @Test
  void contentInternalErrorTest2() throws Exception {
    when(revisionProvider.getByPage(1))
        .thenReturn(
            new ArrayList<>(
                Collections.singleton(new RevisionData(1, 1, 1, new byte[0], null, true))));
    when(versionProvider.getVersion(any())).thenThrow(ie);
    testException("/api/revision/content", Method.POST, id1, ie);
  }

  @Test
  void byIdInternalErrorTest() throws Exception {
    when(revisionProvider.getById(1)).thenThrow(ie);
    testException("/api/revision/byid", Method.POST, id1, ie);
  }

  @Test
  void draftInternalErrorTest() throws Exception {
    when(revisionProvider.getDraft(5, 7)).thenThrow(ie);
    testException("/api/revision/draft", Method.POST, draft1, ie);
  }

  @Test
  void draftInternalErrorTest2() throws Exception {
    when(revisionProvider.getDraft(5, 7)).thenReturn(new byte[0]);
    when(versionProvider.getVersion(any())).thenThrow(ie);
    testException("/api/revision/draft", Method.POST, draft1, ie);
  }

  @Test
  void draftInternalErrorTest3() throws Exception {
    when(revisionProvider.getDraft(5, 7)).thenReturn(new byte[0]);
    when(versionProvider.getVersion(any())).thenReturn(null);
    testException(
        "/api/revision/draft",
        Method.POST,
        draft1,
        new InternalErrorException("Incorrect data id"));
  }

  @Test
  void createBadDataTest() throws Exception {
    when(revisionProvider.createRevision(any())).thenThrow(bad);
    testException("/api/revision/create", Method.PUT, create1, bad);
  }

  @Test
  void createBadDataTest2() throws Exception {
    when(versionProvider.storeVersion(any(), any())).thenThrow(bad);
    testException(
        "/api/revision/create", Method.PUT, create1, new InternalErrorException(bad.getMessage()));
  }

  @Test
  void createInternalErrorTest() throws Exception {
    when(revisionProvider.getByPage(anyInt())).thenThrow(ie);
    testException("/api/revision/create", Method.PUT, create1, ie);
  }

  @Test
  void createInternalErrorTest2() throws Exception {
    when(versionProvider.storeVersion(any(), any())).thenThrow(ie);
    testException("/api/revision/create", Method.PUT, create1, ie);
  }

  @Test
  void createInternalErrorTest3() throws Exception {
    when(revisionProvider.createRevision(any())).thenThrow(ie);
    testException("/api/revision/create", Method.PUT, create1, ie);
  }

  @Test
  void updateStatusBadDataTest() throws Exception {
    doThrow(bad).when(revisionProvider).changeState(1, true);
    testException("/api/revision/update/status", Method.PUT, status1, bad);
  }

  @Test
  void updateStatusInternalErrorTest() throws Exception {
    doThrow(ie).when(revisionProvider).changeState(1, true);
    testException("/api/revision/update/status", Method.PUT, status1, ie);
  }

  @Test
  void draftSaveBadDataTest() throws Exception {
    doThrow(bad).when(revisionProvider).saveDraft(anyInt(), anyInt(), any());
    testException("/api/revision/draft/save", Method.PUT, draftSave1, bad);
  }

  @Test
  void draftSaveBadDataTest2() throws Exception {
    doThrow(bad).when(versionProvider).storeVersion(any(), any());
    testException(
        "/api/revision/draft/save",
        Method.PUT,
        draftSave1,
        new InternalErrorException(bad.getMessage()));
  }

  @Test
  void draftSaveInternalErrorTest() throws Exception {
    doThrow(ie).when(revisionProvider).saveDraft(anyInt(), anyInt(), any());
    testException("/api/revision/draft/save", Method.PUT, draftSave1, ie);
  }

  @Test
  void draftSaveInternalErrorTest2() throws Exception {
    doThrow(ie).when(revisionProvider).getDraft(5, 7);
    testException("/api/revision/draft/save", Method.PUT, draftSave1, ie);
  }

  @Test
  void draftSaveInternalErrorTest3() throws Exception {
    doThrow(ie).when(versionProvider).storeVersion(any(), any());
    testException("/api/revision/draft/save", Method.PUT, draftSave1, ie);
  }

  @Test
  void draftDeleteInternalErrorTest() throws Exception {
    doThrow(ie).when(revisionProvider).deleteDraft(5, 7);
    testException("/api/revision/draft/delete", Method.DELETE, draft1, ie);
  }

  @Test
  void draftDeleteInternalErrorTest2() throws Exception {
    doThrow(ie).when(revisionProvider).getDraft(5, 7);
    testException("/api/revision/draft/delete", Method.DELETE, draft1, ie);
  }

  @Test
  void draftDeleteInternalErrorTest3() throws Exception {
    doThrow(ie).when(versionProvider).deleteVersion(any());
    testException("/api/revision/draft/delete", Method.DELETE, draft1, ie);
  }

  @Test
  void deleteInternalErrorTest() throws Exception {
    doThrow(ie).when(revisionProvider).deleteRevision(1);
    testException("/api/revision/delete", Method.DELETE, id1, ie);
  }
}
