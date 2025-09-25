package dev.tfkls.hikkei.server.routes.error;

import static dev.tfkls.hikkei.server.util.RestCaller.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.server.routes.RouteHandlerIntegrationTest;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErrorCommentRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private InternalErrorException ie;
  private BadDataException bad;
  private JsonObject create;
  private PageData pd;

  @BeforeAll
  void init() {
    ie = new InternalErrorException("testcommentie");
    bad = new BadDataException("testcommentbad");
    create = new JsonObject().put("page_id", 1).put("content", "dsfdss");
    pd = new PageData(1, "", 1);
  }

  @Test
  void userInternalErrorTest() throws Exception {
    when(commentProvider.getByUser(1)).thenThrow(ie);
    testGetException("/api/comment/user/1", ie);
  }

  @Test
  void pageInternalErrorTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(pd);
    when(commentProvider.getByPage(1)).thenThrow(ie);
    testGetException("/api/comment/page/1", ie);
  }

  @Test
  void byIdInternalErrorTest() throws Exception {
    when(commentProvider.getById(1)).thenThrow(ie);
    testGetException("/api/comment/1", ie);
  }

  @Test
  void createInternalErrorTest() throws Exception {
    setupAccessManager();
    when(commentProvider.addComment(any())).thenThrow(ie);
    testException("/api/comment/create", Method.POST, create, ie);
  }

  @Test
  void createBadDataTest() throws Exception {
    setupAccessManager();
    when(commentProvider.addComment(any())).thenThrow(bad);
    testException("/api/comment/create", Method.POST, create, bad);
  }

  @Test
  void deleteInternalError() throws Exception {
    when(commentProvider.getById(1)).thenReturn(new CommentData(1, 1, ""));
    when(pageProvider.getById(1)).thenReturn(pd);
    doThrow(ie).when(commentProvider).deleteComment(1);
    testException("/api/comment/delete", Method.DELETE, new JsonObject().put("id", 1), ie);
  }
}
