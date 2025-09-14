package dev.tfkls.hikkei.provider.internal;

import static dev.tfkls.hikkei.provider.JDBC.Config.internal_error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.provider.JDBC.JDBCCategoryDataProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JDBCCategoryDataProviderInternalErrorTest {
  private JDBCCategoryDataProvider provider;

  @BeforeAll
  void setUp() {
    Connection connection =
        mock(
            Connection.class,
            invocation -> {
              throw new SQLException("test");
            });
    provider = new JDBCCategoryDataProvider(connection);
  }

  @Test
  void createCategoryThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(
            InternalErrorException.class,
            () -> provider.createCategory(new CategoryData(1, "name", LocalDateTime.now())));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getCategoriesThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getCategories());
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getByIdThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getById(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void changeNameThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.changeName(1, "newName"));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void updatePermissionThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.updatePermission(1, 1, null));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void deleteCategoryThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.deleteCategory(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getUsersPermissionsThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getUsersPermissions(1));
    assertEquals(internal_error, ex.getMessage());
  }
}
