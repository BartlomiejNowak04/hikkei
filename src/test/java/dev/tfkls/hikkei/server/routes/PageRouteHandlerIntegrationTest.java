package dev.tfkls.hikkei.server.routes;

import static dev.tfkls.hikkei.server.util.RestCaller.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import io.restassured.http.Method;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalInt;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PageRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private PageData pd;

  @BeforeAll
  void init() {
    LocalDateTime time = LocalDateTime.now();
    pd = new PageData(1, 1, "a", 1, false, time);
  }

  private void assertPage(PageData data, JsonObject body) {
    assertEquals(data.id(), body.getInteger("id"));
    assertEquals(data.userId(), body.getInteger("user_id"));
    assertEquals(data.category(), body.getInteger("category"));
    assertEquals(data.title(), body.getString("title"));
    assertEquals(data.archived(), body.getBoolean("archived"));
    assertEquals(data.created().toString(), body.getString("created"));
  }

  @Test
  void randomNoDataTest() throws Exception {
    when(pageProvider.getRandomPage()).thenReturn(null);
    testGetException("/api/page/random", new InternalErrorException("No pages available"));
  }

  @Test
  void randomWorkingTest() throws Exception {
    when(pageProvider.getRandomPage()).thenReturn(pd);
    JsonObject body = callGetEndpointObject("/api/page/random", 200);
    assertPage(pd, body);
  }

  @Test
  void byTitleNoDataTest() throws Exception {
    when(pageProvider.getByTitle("a")).thenReturn(null);
    testGetException("/api/page/title/a", new BadDataException("Invalid page title: a"));
  }

  @Test
  void byTitleWorkingTest() throws Exception {
    when(pageProvider.getByTitle("a")).thenReturn(pd);
    JsonObject body = callGetEndpointObject("/api/page/title/a", 200);
    assertPage(pd, body);
  }

  @Test
  void byUrlNoDataTest() throws Exception {
    when(pageProvider.getByTitle("a a")).thenReturn(null);
    testGetException("/api/page/a_a", new BadDataException("Invalid page title: a a"));
  }

  @Test
  void byUrlWorkingTest() throws Exception {
    when(pageProvider.getByTitle("a a")).thenReturn(pd);
    JsonObject body = callGetEndpointObject("/api/page/a_a", 200);
    assertPage(pd, body);
  }

  @Test
  void searchNoDataTest() throws Exception {
    when(pageProvider.searchByTitle("a")).thenReturn(List.of());
    JsonArray body = callGetEndpointArray("/api/page/search/a", 200);
    assertEquals(0, body.size());
  }

  @Test
  void searchWorkingTest() throws Exception {
    when(pageProvider.searchByTitle("a")).thenReturn(List.of(pd));
    JsonArray body = callGetEndpointArray("/api/page/search/a", 200);
    assertEquals(1, body.size());
    assertPage(pd, body.getJsonObject(0));
  }

  @Test
  void categoryNoCategoryTest() throws Exception {
    when(pageProvider.getByCategory(1)).thenReturn(null);
    testGetException("/api/page/category/1", new BadDataException("Invalid category id"));
  }

  @Test
  void categoryNoDataTest() throws Exception {
    when(pageProvider.getByCategory(1)).thenReturn(List.of());
    JsonArray body = callGetEndpointArray("/api/page/category/1", 200);
    assertEquals(0, body.size());
  }

  @Test
  void categoryWorkingTest() throws Exception {
    when(pageProvider.getByCategory(1)).thenReturn(List.of(pd));
    JsonArray body = callGetEndpointArray("/api/page/category/1", 200);
    assertEquals(1, body.size());
    assertPage(pd, body.getJsonObject(0));
  }

  @Test
  void createWorkingTest() throws Exception {
    when(accessManager.requireUser(any())).thenReturn(1);
    when(accessManager.fetchUser(any())).thenReturn(OptionalInt.of(1));
    doNothing().when(accessManager).setCategory(any(), anyInt());
    doNothing().when(accessManager).verifyPermission(any(), any());
    when(pageProvider.createPage(any())).thenReturn(1);

    JsonObject body =
        callEndpointObject(
            "/api/page/create",
            Method.POST,
            new JsonObject().put("title", "a").put("category", 1),
            201);
    assertEquals(1, body.getInteger("id"));
  }

  @Test
  void updateTitleWorkingTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint(
        "/api/page/update/title",
        Method.PUT,
        new JsonObject().put("id", 1).put("title", "aa"),
        204,
        "");
  }

  @Test
  void updateCategoryWorkingTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint(
        "/api/page/update/category",
        Method.PUT,
        new JsonObject().put("id", 1).put("category", 2),
        204,
        "");
  }

  @Test
  void updateArchiveWorkingTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint(
        "/api/page/update/archive",
        Method.PUT,
        new JsonObject().put("id", 1).put("value", true),
        204,
        "");
  }

  @Test
  void deleteWorkingTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(pd);
    callEndpoint("/api/page/delete", Method.DELETE, new JsonObject().put("id", 1), 204, "");
  }
}
