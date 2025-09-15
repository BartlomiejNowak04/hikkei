package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.provider.JDBC.Config;
import dev.tfkls.hikkei.provider.JDBC.JDBCCategoryDataProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCCategoryDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCCategoryDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCCategoryDataProvider(connection);
  }

  @Test
  void freshlyAddedCategoryExists() throws Exception {
    CategoryData cat = new CategoryData("newcategory");
    int id = provider.createCategory(cat);
    assertTrue(id > 0);

    try (PreparedStatement st =
        connection.prepareStatement("select * from categories_with_data where id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertEquals("newcategory", rs.getString("name"));
    }
  }

  @Test
  void getCategories_returnsAllNonDeleted() throws Exception {
    int id1 = create_category("allcat1");
    int id2 = create_category("allcat2");
    delete("category", id2);

    List<CategoryData> cats = provider.getCategories();
    assertTrue(cats.stream().anyMatch(c -> c.id() == id1));
    assertFalse(cats.stream().anyMatch(c -> c.id() == id2));
  }

  @Test
  void getByIdTest() throws Exception {
    assertNull(provider.getById(-1));
    int id = create_category("byid");
    CategoryData cat = provider.getById(id);
    assertNotNull(cat);
    assertEquals("byid", cat.name());
    assertEquals(id, cat.id());

    delete("category", id);
    assertEquals(CategoryData.DELETED, provider.getById(id));
  }

  @Test
  void changeName_invalidCases() {
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.changeName(1, null));
    assertEquals(Config.invalid_name, ex.getMessage());

    BadDataException ex2 =
        assertThrows(BadDataException.class, () -> provider.changeName(-1, "something"));
    assertEquals(Config.invalid_category, ex2.getMessage());
  }

  @Test
  void changeName_success() throws Exception {
    int id = create_category("oldname");
    provider.changeName(id, "newname");

    try (PreparedStatement st =
        connection.prepareStatement("select * from categories_with_data where id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertEquals("newname", rs.getString("name"));
    }
  }

  @Test
  void updatePermission_invalidCategoryOrUser() throws Exception {
    InternalErrorException ex =
        assertThrows(
            InternalErrorException.class,
            () -> provider.updatePermission(-1, 1, PermissionLevel.ADMIN));
    assertEquals(Config.invalid_category, ex.getMessage());

    int catId = create_category("permcat");
    InternalErrorException ex2 =
        assertThrows(
            InternalErrorException.class,
            () -> provider.updatePermission(catId, -1, PermissionLevel.TRUSTED));
    assertEquals(Config.invalid_user, ex2.getMessage());
  }

  @Test
  void updatePermission_andGetUsersPermissions_success() throws Exception {
    int catId = create_category("permcat2");
    int userId = create_user("permuser", null);

    provider.updatePermission(catId, userId, PermissionLevel.MODERATOR);
    Map<Integer, PermissionLevel> perms = provider.getUsersPermissions(catId);
    assertTrue(perms.containsKey(userId));
    assertEquals(PermissionLevel.MODERATOR, perms.get(userId));
  }

  @Test
  void getUsersPermissions_invalidCategory_returnsNull() throws Exception {
    assertNull(provider.getUsersPermissions(-1));
  }

  @Test
  void deleteCategory_marksDeleted() throws Exception {
    int id = create_category("todelete");
    provider.deleteCategory(id);

    try (PreparedStatement st =
        connection.prepareStatement("select * from categories where id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertTrue(rs.getBoolean("deleted"));
    }
  }
}
