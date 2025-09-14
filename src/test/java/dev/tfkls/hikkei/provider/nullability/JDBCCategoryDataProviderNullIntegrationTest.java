package dev.tfkls.hikkei.provider.nullability;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.provider.JDBC.Config;
import dev.tfkls.hikkei.provider.JDBC.JDBCCategoryDataProvider;
import dev.tfkls.hikkei.provider.JDBCIntegrationTest;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCCategoryDataProviderNullIntegrationTest extends JDBCIntegrationTest {
  private CategoryDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCCategoryDataProvider(connection);
  }

  @Test
  void createCategory_nullCategoryData_throwsBadDataException() {
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createCategory(null));
    assertEquals(Config.invalid_data, ex.getMessage());
  }

  @Test
  void createCategory_nullName_throwsBadDataException() {
    CategoryData data = new CategoryData(0, null, LocalDateTime.now());
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createCategory(data));
    assertEquals(Config.invalid_name, ex.getMessage());
  }

  @Test
  void getById_notExisting_returnsNull() throws InternalErrorException {
    assertNull(provider.getById(-1));
  }

  @Test
  void changeName_nullName_throwsBadDataException() {
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.changeName(1, null));
    assertEquals(Config.invalid_name, ex.getMessage());
  }

  @Test
  void updatePermission_nullLevel_allowsNullPermission() throws SQLException {
    int catId = create_category("cnullperm");
    int userId = create_user("ucnull", null);

    assertDoesNotThrow(() -> provider.updatePermission(catId, userId, null));
  }

  @Test
  void getUsersPermissions_invalidCategory_returnsNull() throws InternalErrorException {
    assertNull(provider.getUsersPermissions(-1));
  }
}
