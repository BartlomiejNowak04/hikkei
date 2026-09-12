package dev.tfkls.hikkei.server.routes;

import static dev.tfkls.hikkei.server.util.RestCaller.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import io.restassured.http.Method;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommentRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private CommentData cd;
  private PageData pd;

  @BeforeAll
  void init() {
    cd = new CommentData(1, 1, 1, "test", LocalDateTime.now());
    pd = new PageData(1, "test", 1);
  }

  private void assertComment(JsonObject body, CommentData comment) {
    assertEquals(comment.id(), body.getInteger("id"));
    assertEquals(comment.pageId(), body.getInteger("page_id"));
    assertEquals(comment.userId(), body.getInteger("user_id"));
    assertEquals(comment.content(), body.getString("content"));
    assertEquals(comment.date().toString(), body.getString("date"));
  }

  @Test
  void userNoUserTest() throws Exception {
    when(commentProvider.getByUser(1)).thenReturn(null);
    testGetException("/api/comment/user/1", new BadDataException("Incorrect parameter value: id"));
  }

  @Test
  void userWorkingTest() throws Exception {
    when(commentProvider.getByUser(1)).thenReturn(List.of(cd));
    JsonArray body = callGetEndpointArray("api/comment/user/1", 200);
    assertEquals(1, body.size());
    assertComment(body.getJsonObject(0), cd);
  }

  @Test
  void pageNoPageTest() throws Exception {
    when(commentProvider.getByPage(1)).thenReturn(null);
    when(pageProvider.getById(1)).thenReturn(pd);
    testGetException("/api/comment/page/1", new BadDataException("Incorrect parameter value: id"));
  }

  @Test
  void pageWorkingTest() throws Exception {
    when(commentProvider.getByPage(1)).thenReturn(List.of(cd));
    when(pageProvider.getById(1)).thenReturn(pd);
    JsonArray body = callGetEndpointArray("api/comment/page/1", 200);
    assertEquals(1, body.size());
    assertComment(body.getJsonObject(0), cd);
  }

  @Test
  void byIdNoCommentTest() throws Exception {
    when(commentProvider.getById(1)).thenReturn(null);
    testGetException("/api/comment/1", new BadDataException("Incorrect parameter value: id"));
  }

  @Test
  void byIdWorkingTest() throws Exception {
    when(commentProvider.getById(1)).thenReturn(cd);
    when(pageProvider.getById(1)).thenReturn(pd);
    JsonObject body = callGetEndpointObject("/api/comment/1", 200);
    assertComment(body, cd);
  }

  @Test
  void createWorkingTest() throws Exception {
    when(accessManager.requireUser(any())).thenReturn(1);
    when(accessManager.fetchUser(any())).thenReturn(Optional.of(1));
    doNothing().when(accessManager).setCategory(any(), anyInt());
    doNothing().when(accessManager).verifyPermission(any(), any());

    when(pageProvider.getById(1)).thenReturn(pd);
    when(commentProvider.addComment(any())).thenReturn(1);

    JsonObject body =
        callEndpointObject(
            "/api/comment/create",
            Method.POST,
            new JsonObject().put("page_id", 1).put("content", "gfdd"),
            201);
    assertEquals(1, body.getInteger("id"));
  }

  @Test
  void deleteWorkingTest() throws Exception {
    when(commentProvider.getById(1)).thenReturn(cd);
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint("/api/comment/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
  }

  @Test
  void deleteByAuthorNeedsNoCategoryPermission() throws Exception {
    when(accessManager.fetchUser(any())).thenReturn(Optional.of(cd.userId()));
    when(commentProvider.getById(1)).thenReturn(cd);
    callEndpoint("/api/comment/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
    verify(accessManager, never()).verifyPermission(any(), any());
    verify(commentProvider).deleteComment(1);
  }

  @Test
  void deleteByOtherUserRequiresModerator() throws Exception {
    when(accessManager.fetchUser(any())).thenReturn(Optional.of(cd.userId() + 1));
    when(commentProvider.getById(1)).thenReturn(cd);
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint("/api/comment/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
    verify(accessManager).setCategory(any(), eq(pd.category()));
    verify(accessManager).verifyPermission(any(), eq(PermissionLevel.MODERATOR));
  }

  @Test
  void deleteAnonymouslyRequiresModerator() throws Exception {
    when(accessManager.fetchUser(any())).thenReturn(Optional.empty());
    when(commentProvider.getById(1)).thenReturn(cd);
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint("/api/comment/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
    verify(accessManager).verifyPermission(any(), eq(PermissionLevel.MODERATOR));
  }

  @Test
  void deleteNoCommentTest() throws Exception {
    when(commentProvider.getById(1)).thenReturn(null);
    testException(
        "/api/comment/delete",
        Method.DELETE,
        new JsonObject().put("id", 1),
        new BadDataException("Incorrect parameter value: id"));
    verify(commentProvider, never()).deleteComment(anyInt());
  }

  @Test
  void deleteDeletedCommentTest() throws Exception {
    when(commentProvider.getById(1)).thenReturn(CommentData.DELETED);
    callEndpoint("/api/comment/delete", Method.DELETE, new JsonObject().put("id", 1), 404, "");
    verify(commentProvider, never()).deleteComment(anyInt());
  }
}
