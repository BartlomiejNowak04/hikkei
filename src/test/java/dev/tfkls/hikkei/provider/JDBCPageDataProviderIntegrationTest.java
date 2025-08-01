package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.revision.RevisionData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCPageDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCPageDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCPageDataProvider(connection);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO categories (id, name) VALUES (?, ?)")) {
      st.setInt(1, 1);
      st.setString(2, "TestCategory");
      st.executeUpdate();
    } catch (SQLException ignored) {
    }
  }

  @Test
  void nonExistentPageDoesNotExist() throws InternalErrorException {
    assertFalse(provider.doesPageExist(-1));
  }

  @Test
  void createAndRetrievePage() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, "Test Title", 1, false);
    Integer pageId = provider.createPage(page);
    assertNotNull(pageId);
    assertTrue(provider.doesPageExist(pageId));

    PageData retrieved = provider.getPageData(pageId);
    assertEquals("Test Title", retrieved.title());
    assertEquals(1, retrieved.category());
    assertFalse(retrieved.archived());
  }

  @Test
  void getPageByTitleAndSearch() throws InternalErrorException, BadDataException {
    String title = "UniqueTitle123";
    PageData page = new PageData(null, title, 1, false);
    provider.createPage(page);

    PageData byTitle = provider.getPageByTitle(title);
    assertNotNull(byTitle);
    assertEquals(title, byTitle.title());

    List<PageData> results = provider.searchPageByTitle("UniqueTitle");
    assertFalse(results.isEmpty());
    assertTrue(results.stream().anyMatch(p -> p.title().equals(title)));
  }

  @Test
  void updateTitleAndCategory() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, "OldTitle", 1, false);
    Integer pageId = provider.createPage(page);

    provider.updatePageTitle(pageId, "NewTitle");
    assertEquals("NewTitle", provider.getPageData(pageId).title());

    provider.updatePageCategory(pageId, 1);
    assertEquals(1, provider.getPageData(pageId).category());
  }

  @Test
  void toggleArchiveState() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, "Archivable", 1, false);
    Integer pageId = provider.createPage(page);

    provider.toggleArchive(pageId, true);
    assertTrue(provider.getPageData(pageId).archived());

    provider.toggleArchive(pageId, false);
    assertFalse(provider.getPageData(pageId).archived());
  }

  @Test
  void getRandomPageReturnsNonNull() throws InternalErrorException, BadDataException {
    provider.createPage(new PageData(null, "RandomTestPage", 1, false));
    assertNotNull(provider.getRandomPage());
  }

  @Test
  void pageWithNoRevisionsReturnsEmptyList() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, "NoRevisions", 1, false);
    Integer pageId = provider.createPage(page);
    List<RevisionData> revisions = provider.getPageRevisions(pageId);
    assertNotNull(revisions);
    assertTrue(revisions.isEmpty());
  }

  @Test
  void pageWithNoCommentsReturnsEmptyList() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, "NoComments", 1, false);
    Integer pageId = provider.createPage(page);
    List<CommentData> comments = provider.getPageComments(pageId);
    assertNotNull(comments);
    assertTrue(comments.isEmpty());
  }

  @Test
  void invalidPageUpdatesThrowExceptions() {
    assertThrows(BadDataException.class, () -> provider.updatePageTitle(-1, "Invalid"));
    assertThrows(BadDataException.class, () -> provider.updatePageCategory(-1, 1));
    assertThrows(BadDataException.class, () -> provider.toggleArchive(-1, true));
  }

  @Test
  void duplicateTitleThrows() throws InternalErrorException, BadDataException {
    PageData page = new PageData(null, "DuplicateTitle", 1, false);
    provider.createPage(page);
    assertThrows(BadDataException.class, () -> provider.createPage(page));
  }

  @Test
  void invalidCategoryThrows() {
    PageData page = new PageData(null, "BadCategory", -1, false);
    assertThrows(BadDataException.class, () -> provider.createPage(page));
  }
}
