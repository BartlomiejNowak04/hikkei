package dev.tfkls.hikkei.server.routes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.page.PageData;
import io.restassured.http.Method;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.List;
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

  //  @Test
  //  void createWorkingTest() throws Exception {
  //    when(commentProvider.addComment(any())).thenReturn(1);
  //    JsonObject body = callEndpointObject("/api/comment/create", Method.POST, new
  // JsonObject().put("page_id", 1).put("content", "gfdd"), 201);
  //    assertEquals(1, body.getInteger("id"));
  //  }

  @Test
  void deleteWorkingTest() throws Exception {
    when(commentProvider.getById(1)).thenReturn(cd);
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint("/api/comment/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
  }
}
