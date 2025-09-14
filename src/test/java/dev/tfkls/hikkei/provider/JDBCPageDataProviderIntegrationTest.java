package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.provider.JDBC.JDBCPageDataProvider;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCPageDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCPageDataProvider provider;

  @BeforeAll
  void initProvider() throws SQLException {
    provider = new JDBCPageDataProvider(connection);

    create_category("test-category");
    create_user("testname", null);
  }

  @Test
  void nonExistentPageDoesNotExist() throws InternalErrorException {
    assertFalse(provider.doesPageExist(-1));
  }

  @Test
  void createAndRetrievePage() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, 1, "test", 1, false, null);
    int pageId = provider.createPage(page);
    assertTrue(provider.doesPageExist(pageId));

    PageData retrieved = provider.getById(pageId);
    assertEquals(page.title(), retrieved.title());
    assertEquals(page.category(), retrieved.category());
    assertFalse(retrieved.archived());
  }

  @Test
  void getPageByTitleAndSearch() throws InternalErrorException, BadDataException {
    String title = "UniqueTitle123";
    PageData page = new PageData(null, 1, title, 1, false, null);
    provider.createPage(page);

    PageData byTitle = provider.getByTitle(title);
    assertNotNull(byTitle);
    assertEquals(title, byTitle.title());

    List<PageData> results = provider.searchByTitle("UniqueTitle");
    assertFalse(results.isEmpty());
    assertTrue(results.stream().anyMatch(p -> p.title().equals(title)));
  }

  @Test
  void updateTitleAndCategory() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, 1, "OldTitle", 1, false, null);
    int pageId = provider.createPage(page);

    provider.updatePageTitle(pageId, "NewTitle");
    assertEquals("NewTitle", provider.getById(pageId).title());

    provider.updatePageCategory(pageId, 1);
    assertEquals(1, provider.getById(pageId).category());
  }

  @Test
  void toggleArchiveState() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, 1, "Archivable", 1, false, null);
    int pageId = provider.createPage(page);

    provider.toggleArchive(pageId, true);
    assertTrue(provider.getById(pageId).archived());

    provider.toggleArchive(pageId, false);
    assertFalse(provider.getById(pageId).archived());
  }

  @Test
  void getRandomPageReturnsNonNull() throws InternalErrorException, BadDataException {
    provider.createPage(new PageData(null, 1, "RandomTestPage", 1, false, null));
    assertNotNull(provider.getRandomPage());
  }

  @Test
  void invalidPageUpdatesThrowExceptions() {
    assertThrows(BadDataException.class, () -> provider.updatePageTitle(-1, "Invalid"));
    assertThrows(BadDataException.class, () -> provider.updatePageCategory(-1, 1));
    assertThrows(BadDataException.class, () -> provider.toggleArchive(-1, true));
  }

  @Test
  void duplicateTitleThrows() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, 1, "DuplicateTitle", 1, false, null);
    provider.createPage(page);
    assertThrows(BadDataException.class, () -> provider.createPage(page));
  }

  @Test
  void invalidCategoryThrows() {
    PageData page = new PageData(null, 1, "BadCategory", -1, false, null);
    assertThrows(BadDataException.class, () -> provider.createPage(page));
  }
}
