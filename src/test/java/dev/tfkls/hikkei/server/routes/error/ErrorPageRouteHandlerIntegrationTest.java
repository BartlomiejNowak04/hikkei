package dev.tfkls.hikkei.server.routes.error;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.server.routes.RouteHandlerIntegrationTest;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErrorPageRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private InternalErrorException ie;
  private BadDataException bad;
  private JsonObject id1;
  private JsonObject title1;
  private JsonObject url1;
  private JsonObject category1;
  private JsonObject create1;
  private JsonObject archive1;

  @BeforeAll
  void init() {
    ie = new InternalErrorException("testpageie111");
    bad = new BadDataException("badpage222");
    id1 = new JsonObject().put("id", 1);
    title1 = new JsonObject().put("title", "sometitle");
    url1 = new JsonObject().put("url", "https://example.com");
    category1 = new JsonObject().put("category", 10);
    create1 = new JsonObject().put("title", "pagetitle").put("category", 10).put("user", 5);
    archive1 = new JsonObject().put("id", 1).put("value", true);
  }

  @Test
  void randomInternalErrorTest() throws Exception {
    when(pageProvider.getRandomPage()).thenThrow(ie);
    testException("/api/page/random", Method.POST, null, ie);
  }

  @Test
  void byTitleInternalErrorTest() throws Exception {
    when(pageProvider.getByTitle("sometitle")).thenThrow(ie);
    testException("/api/page/bytitle", Method.POST, title1, ie);
  }

  @Test
  void byUrlInternalErrorTest() throws Exception {
    when(pageProvider.getByTitle("https://example.com")).thenThrow(ie);
    testException("/api/page/byurl", Method.POST, url1, ie);
  }

  @Test
  void searchInternalErrorTest() throws Exception {
    when(pageProvider.searchByTitle("sometitle")).thenThrow(ie);
    testException("/api/page/search", Method.POST, title1, ie);
  }

  @Test
  void categoryInternalErrorTest() throws Exception {
    when(pageProvider.getByCategory(10)).thenThrow(ie);
    testException("/api/page/category", Method.POST, category1, ie);
  }

  @Test
  void createBadDataTest() throws Exception {
    when(pageProvider.createPage(any())).thenThrow(bad);
    testException("/api/page/create", Method.PUT, create1, bad);
  }

  @Test
  void createInternalErrorTest() throws Exception {
    when(pageProvider.createPage(any())).thenThrow(ie);
    testException("/api/page/create", Method.PUT, create1, ie);
  }

  @Test
  void updateTitleBadDataTest() throws Exception {
    doThrow(bad).when(pageProvider).updatePageTitle(1, "sometitle");
    testException("/api/page/update/title", Method.PUT, id1.copy().put("title", "sometitle"), bad);
  }

  @Test
  void updateTitleInternalErrorTest() throws Exception {
    doThrow(ie).when(pageProvider).updatePageTitle(1, "sometitle");
    testException("/api/page/update/title", Method.PUT, id1.copy().put("title", "sometitle"), ie);
  }

  @Test
  void updateCategoryBadDataTest() throws Exception {
    doThrow(bad).when(pageProvider).updatePageCategory(1, 10);
    testException("/api/page/update/category", Method.PUT, id1.copy().put("category", 10), bad);
  }

  @Test
  void updateCategoryInternalErrorTest() throws Exception {
    doThrow(ie).when(pageProvider).updatePageCategory(1, 10);
    testException("/api/page/update/category", Method.PUT, id1.copy().put("category", 10), ie);
  }

  @Test
  void updateArchiveBadDataTest() throws Exception {
    doThrow(bad).when(pageProvider).toggleArchive(1, true);
    testException("/api/page/update/archive", Method.PUT, archive1, bad);
  }

  @Test
  void updateArchiveInternalErrorTest() throws Exception {
    doThrow(ie).when(pageProvider).toggleArchive(1, true);
    testException("/api/page/update/archive", Method.PUT, archive1, ie);
  }

  @Test
  void deleteInternalErrorTest() throws Exception {
    doThrow(ie).when(pageProvider).deletePage(1);
    testException("/api/page/delete", Method.DELETE, id1, ie);
  }
}
