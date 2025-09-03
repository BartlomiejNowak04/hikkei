package dev.tfkls.hikkei.provider.nullability;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.page.PageDataProvider;
import dev.tfkls.hikkei.provider.JDBC.Config;
import dev.tfkls.hikkei.provider.JDBC.JDBCPageDataProvider;
import dev.tfkls.hikkei.provider.JDBCIntegrationTest;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCPageDataProviderNullIntegrationTest extends JDBCIntegrationTest {
  private PageDataProvider provider;

  @BeforeAll
  void initProvider() throws SQLException {
    provider = new JDBCPageDataProvider(connection);

    create_user("tuser", null);
    create_category("tcat");
  }

  @Test
  void getByTitle_nullTitle_returnsNull() throws Exception {
    assertNull(provider.getByTitle(null));
  }

  @Test
  void searchByTitle_nullTitle_returnsEmpty() throws Exception {
    assertTrue(provider.searchByTitle(null).isEmpty());
  }

  @Test
  void createPage_nullPageData_throwsBadDataException() {
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createPage(null));
    assertEquals(Config.invalid_data, ex.getMessage());
  }

  @Test
  void createPage_nullTitle_throwsBadDataException() {
    PageData page = new PageData(1, null, 1, false);
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createPage(page));
    assertEquals(Config.invalid_title, ex.getMessage());
  }

  @Test
  void createPage_nullCategory_throwsBadDataException() {
    PageData page = new PageData(1, "Valid Title", null, false);
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createPage(page));
    assertEquals(Config.invalid_category, ex.getMessage());
  }

  @Test
  void updatePageTitle_nullTitle_throwsBadDataException() throws Exception {
    int id = create_page("aa", create_category("aa"), create_user("kdlsjfkdsjfsd", null));
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.updatePageTitle(id, null));
    assertEquals(Config.invalid_title, ex.getMessage());
  }
}
