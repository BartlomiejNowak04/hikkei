package dev.tfkls.hikkei.provider.internal;

import static dev.tfkls.hikkei.provider.JDBC.Config.internal_error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.provider.JDBC.JDBCPageDataProvider;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDBCPageDataProviderInternalErrorTest {
  private JDBCPageDataProvider provider;

  @BeforeAll
  void setUp() {
    Connection connection =
        mock(
            Connection.class,
            invocation -> {
              throw new SQLException("test");
            });
    provider = new JDBCPageDataProvider(connection);
  }

  @Test
  void doesPageExistThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.doesPageExist(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getRandomPageThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getRandomPage());
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void searchByTitleThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.searchByTitle("title"));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getByIdThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getById(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getByTitleThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getByTitle("title"));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getByCategoryThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getByCategory(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void createPageThrowsInternalError() throws Exception {
    PageData page = new PageData(1, 1, "title", 1, false, null);
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.createPage(page));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void updatePageTitleThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.updatePageTitle(1, "new"));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void updatePageCategoryThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.updatePageCategory(1, 2));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void toggleArchiveThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.toggleArchive(1, true));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void deletePageThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.deletePage(1));
    assertEquals(internal_error, ex.getMessage());
  }
}
