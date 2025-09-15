package dev.tfkls.hikkei.provider;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;
import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.provider.JDBC.Config;
import dev.tfkls.hikkei.provider.JDBC.JDBCUserDataProvider;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCUserDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCUserDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCUserDataProvider(connection);
  }

  @Test
  void nonExistentUserDoesNotExist() throws InternalErrorException {
    assertFalse(provider.doesUserExist(-1));
  }

  @Test
  void deletedUserDoesNotExist() throws Exception {
    int n = create_user("deleted", null);
    delete("user", n);
    assertFalse(provider.doesUserExist(n));
  }

  @Test
  void existingUserExists() throws Exception {
    int n = create_user("exists", null);
    assertTrue(provider.doesUserExist(n));
  }

  @Test
  void getUserByAuthTest() throws Exception {
    AuthData auth = new AuthData("google", "abc");
    assertNull(provider.getUserByAuth(auth));
    int id = create_user("at", null);
    try (PreparedStatement st =
        connection.prepareStatement("insert into auth (user_id, type, token) values (?, ?, ?)")) {
      st.setInt(1, id);
      st.setString(2, auth.type());
      st.setString(3, auth.token());
      st.executeUpdate();
    }
    UserData user = provider.getUserByAuth(auth);
    assertEquals(id, user.id());
    assertEquals("at", user.username());
    delete("user", id);
    assertEquals(UserData.DELETED, provider.getUserByAuth(auth));
  }

  @Test
  void getUserDataTest() throws Exception {
    assertNull(provider.getUserData(-1));
    int userId = create_user("datatest", null);
    UserData userData = provider.getUserData(userId);
    assertNotNull(userData);
    assertEquals("datatest", userData.username());
    assertEquals(userId, userData.id());
    delete("user", userId);
    assertEquals(UserData.DELETED, provider.getUserData(userId));
  }

  @Test
  void getUserAuthMethodsTest() throws Exception {
    assertNull(provider.getUserAuthMethods(-1));
    int id = create_user("getauth", null);
    assertTrue(provider.getUserAuthMethods(id).isEmpty());

    try (PreparedStatement st =
        connection.prepareStatement(
            "insert into auth (user_id, type, token) values (?, 'a', 'bb')")) {
      st.setInt(1, id);
      st.executeUpdate();
      List<AuthData> lst = provider.getUserAuthMethods(id);
      assertEquals(1, lst.size());
      assertEquals("a", lst.getFirst().type());
      assertEquals("bb", lst.getFirst().token());
    }
  }

  @Test
  void freshlyAddedUserExists() throws Exception {
    UserData user = new UserData("tt", new byte[] {1, 2});
    int userId = provider.createUser(user);
    assertTrue(userId > 0);

    try (PreparedStatement st =
        connection.prepareStatement("select * from users_with_data where id = ?")) {
      st.setInt(1, userId);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertEquals(userId, rs.getInt("id"));
      assertEquals("tt", rs.getString("username"));
      assertArrayEquals(new byte[] {1, 2}, rs.getBytes("picture"));
    }
  }

  @Test
  void noDuplicateUsernames() throws Exception {
    int id = create_user("taken", null);
    BadDataException e =
        assertThrows(BadDataException.class, () -> provider.createUser(new UserData("taken")));
    assertEquals(Config.taken_name, e.getMessage());

    delete("user", id);
    id = provider.createUser(new UserData("taken"));
    try (PreparedStatement st =
        connection.prepareStatement("select * from users_with_data where id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertEquals("taken", rs.getString("username"));
    }
  }

  @Test
  void updateUsernameTest() throws Exception {
    BadDataException e =
        assertThrows(BadDataException.class, () -> provider.updateUsername(-1, "dfdsfd"));
    assertEquals(Config.invalid_user, e.getMessage());

    int id = create_user("tobechanged", null);
    provider.updateUsername(id, "different");
    try (PreparedStatement st =
        connection.prepareStatement("select * from users_with_data where id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertEquals("different", rs.getString("username"));
    }

    int idd = create_user("secondupdate", null);
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.updateUsername(idd, "different"));
    assertEquals(Config.taken_name, ex.getMessage());

    delete("user", id);
    e = assertThrows(BadDataException.class, () -> provider.updateUsername(id, "dfdsfd"));
    assertEquals(Config.invalid_user, e.getMessage());
  }

  @Test
  void updateUserPicture() throws Exception {
    BadDataException e =
        assertThrows(BadDataException.class, () -> provider.updateUserPicture(-1, new byte[] {}));
    assertEquals(Config.invalid_user, e.getMessage());

    int id = create_user("changepicture", null);
    provider.updateUserPicture(id, new byte[] {1, 2, 3});
    try (PreparedStatement st =
        connection.prepareStatement("select * from users_with_data where id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertArrayEquals(new byte[] {1, 2, 3}, rs.getBytes("picture"));
    }

    delete("user", id);
    e = assertThrows(BadDataException.class, () -> provider.updateUsername(id, "dfdsfd"));
    assertEquals(Config.invalid_user, e.getMessage());
  }

  @Test
  void deleteUserTest() throws Exception {
    int id = create_user("todelete", null);
    provider.deleteUser(id);
    try (PreparedStatement st =
        connection.prepareStatement("select * from users_with_data where id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertTrue(rs.getBoolean("deleted"));
    }
    provider.deleteUser(id);
  }

  @Test
  void addUserAuthMethodTest() throws Exception {
    BadDataException e =
        assertThrows(
            BadDataException.class, () -> provider.addUserAuthMethod(-1, new AuthData("a", "b")));
    assertEquals(Config.invalid_user, e.getMessage());

    int id = create_user("toaddauth", null);
    provider.addUserAuthMethod(id, new AuthData("a", "b"));
    try (PreparedStatement st =
        connection.prepareStatement("select * from auth where user_id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertTrue(rs.next());
      assertEquals("a", rs.getString("type"));
      assertEquals("b", rs.getString("token"));
      assertFalse(rs.next());
    }

    BadDataException ex =
        assertThrows(
            BadDataException.class, () -> provider.addUserAuthMethod(id, new AuthData("a", "c")));
    assertEquals(Config.taken_type, ex.getMessage());

    delete("user", id);
    e =
        assertThrows(
            BadDataException.class, () -> provider.addUserAuthMethod(-1, new AuthData("a", "b")));
    assertEquals(Config.invalid_user, e.getMessage());
  }

  @Test
  void removeUserAuthMethodTest() throws Exception {
    BadDataException e =
        assertThrows(BadDataException.class, () -> provider.removeUserAuthMethod(-1, "a"));
    assertEquals(Config.invalid_user, e.getMessage());

    int id = create_user("toaddauthhh", null);
    provider.removeUserAuthMethod(id, "a");

    try (PreparedStatement st =
        connection.prepareStatement(
            "insert into auth (user_id, type, token) values (?, 'a', 'bbbbb')")) {
      st.setInt(1, id);
      st.executeUpdate();
    }
    provider.removeUserAuthMethod(id, "a");
    try (PreparedStatement st =
        connection.prepareStatement("select * from auth where user_id = ?")) {
      st.setInt(1, id);
      ResultSet rs = st.executeQuery();
      assertFalse(rs.next());
    }

    delete("user", id);
    e = assertThrows(BadDataException.class, () -> provider.removeUserAuthMethod(-1, "a"));
    assertEquals(Config.invalid_user, e.getMessage());
  }

  @Test
  void getCategoriesPermissionReturnsNullForNonexistentUser() throws Exception {
    assertNull(provider.getCategoriesPermissions(-1));
  }

  @Test
  void getCategoriesPermissionSuccess() throws Exception {
    int userId = create_user("gcps", null);
    int catId = create_category("gcps");

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO user_category(user_id, category_id, permission) VALUES (?, ?, ?::PermissionLevel)")) {
      st.setInt(1, userId);
      st.setInt(2, catId);
      st.setString(3, PermissionLevel.ADMIN.name());
      st.executeUpdate();
    } catch (SQLException ignored) {
    }

    Map<Integer, PermissionLevel> permissions = provider.getCategoriesPermissions(userId);
    assertEquals(1, permissions.size());
    assertTrue(permissions.containsKey(catId));
    assertEquals(PermissionLevel.ADMIN, permissions.get(catId));
  }
}
