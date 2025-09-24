package dev.tfkls.hikkei.server.routes.error;

import static dev.tfkls.hikkei.server.util.RestCaller.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.server.routes.RouteHandlerIntegrationTest;
import io.restassured.http.Method;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErrorPageRouteHandlerIntegrationTest extends RouteHandlerIntegrationTest {
  private InternalErrorException ie;
  private BadDataException bad;
  private JsonObject title;
  private JsonObject category;
  private JsonObject create1;
  private JsonObject archive1;

  @BeforeAll
  void init() {
    ie = new InternalErrorException("testpageie111");
    bad = new BadDataException("badpage222");
    title = new JsonObject().put("id", 1).put("title", "sometitle");
    category = new JsonObject().put("id", 1).put("category", 10);
    create1 = new JsonObject().put("title", "pagetitle").put("category", 10);
    archive1 = new JsonObject().put("id", 1).put("value", true);
  }

  @Test
  void randomInternalErrorTest() throws Exception {
    when(pageProvider.getRandomPage()).thenThrow(ie);
    testGetException("/api/page/random", ie);
  }

  @Test
  void byTitleInternalErrorTest() throws Exception {
    when(pageProvider.getByTitle("sometitle")).thenThrow(ie);
    testGetException("/api/page/title/sometitle", ie);
  }

  @Test
  void byUrlInternalErrorTest() throws Exception {
    when(pageProvider.getByTitle("example")).thenThrow(ie);
    testGetException("/api/page/example", ie);
  }

  @Test
  void searchInternalErrorTest() throws Exception {
    when(pageProvider.searchByTitle("sometitle")).thenThrow(ie);
    testGetException("/api/page/search/sometitle", ie);
  }

  @Test
  void categoryInternalErrorTest() throws Exception {
    when(pageProvider.getByCategory(10)).thenThrow(ie);
    testGetException("/api/page/category/10", ie);
  }

  //  @Test
  //  void createBadDataTest() throws Exception {
  //    when(pageProvider.createPage(any())).thenThrow(bad);
  //    testException("/api/page/create", Method.POST, create1, bad);
  //  }
  //
  //  @Test
  //  void createInternalErrorTest() throws Exception {
  //    when(pageProvider.createPage(any())).thenThrow(ie);
  //    testException("/api/page/create", Method.POST, create1, ie);
  //  }

  @Test
  void updateTitleBadDataTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "", 2));
    doThrow(bad).when(pageProvider).updatePageTitle(1, "sometitle");
    testException("/api/page/update/title", Method.PUT, title, bad);
  }

  @Test
  void updateTitleInternalErrorTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "", 2));
    doThrow(ie).when(pageProvider).updatePageTitle(1, "sometitle");
    testException("/api/page/update/title", Method.PUT, title, ie);
  }

  @Test
  void updateCategoryBadDataTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "", 2));
    doThrow(bad).when(pageProvider).updatePageCategory(1, 10);
    testException("/api/page/update/category", Method.PUT, category, bad);
  }

  @Test
  void updateCategoryInternalErrorTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "", 2));
    doThrow(ie).when(pageProvider).updatePageCategory(1, 10);
    testException("/api/page/update/category", Method.PUT, category, ie);
  }

  @Test
  void updateArchiveBadDataTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "", 2));
    doThrow(bad).when(pageProvider).toggleArchive(1, true);
    testException("/api/page/update/archive", Method.PUT, archive1, bad);
  }

  @Test
  void updateArchiveInternalErrorTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "", 2));
    doThrow(ie).when(pageProvider).toggleArchive(1, true);
    testException("/api/page/update/archive", Method.PUT, archive1, ie);
  }

  @Test
  void deleteInternalErrorTest() throws Exception {
    when(pageProvider.getById(1)).thenReturn(new PageData(1, "", 2));
    doThrow(ie).when(pageProvider).deletePage(1);
    testException("/api/page/delete", Method.DELETE, new JsonObject().put("id", 1), ie);
  }
}
