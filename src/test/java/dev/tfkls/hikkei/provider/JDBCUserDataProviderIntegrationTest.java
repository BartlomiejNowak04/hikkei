package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.provider.JDBC.JDBCUserDataProvider;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCUserDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCUserDataProvider provider;

  @BeforeAll
  void initProvider() throws SQLException {
    provider = new JDBCUserDataProvider(connection);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO roles_with_data (name) VALUES (?)")) {
      st.setString(1, "test");
      st.executeUpdate();
    }

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO users_with_data (username) VALUES (?)")) {
      st.setString(1, "testname");
      st.executeUpdate();
    }
  }

  @Test
  void nonExistentUserDoesNotExist() throws InternalErrorException {
    assertFalse(provider.doesUserExist(-1));
  }

  @Test
  void freshlyAddedUserExists() throws InternalErrorException, BadDataException {
    UserData user = new UserData("tt", null, LocalDateTime.now());
    int userId = provider.createUser(user);
    assertTrue(userId > 0);
    assertTrue(provider.doesUserExist(userId));
  }

  @Test
  void basicAuthTest() throws InternalErrorException, BadDataException {
    UserData user = new UserData("auth", null, LocalDateTime.now());
    int userId = provider.createUser(user);
    AuthData auth = new AuthData("google", "abc");
    assertNull(provider.getUserByAuth(auth));
    assertThrows(BadDataException.class, () -> provider.addUserAuthMethod(-1, auth));
    assertTrue(provider.addUserAuthMethod(userId, auth));
    assertNotNull(provider.getUserByAuth(auth));
  }

  @Test
  void getUserAuthMethodsTest() throws InternalErrorException, BadDataException {

    assertNull(provider.getUserAuthMethods(-1));
    assertThrows(BadDataException.class, () -> provider.removeUserAuthMethod(-1, ""));

    UserData user = new UserData("authmethods", null, LocalDateTime.now());
    int userId = provider.createUser(user);
    assertTrue(provider.getUserAuthMethods(userId).isEmpty());
    AuthData auth = new AuthData("google", "abcd");
    assertFalse(provider.removeUserAuthMethod(userId, auth.type()));
    provider.addUserAuthMethod(userId, auth);
    assertFalse(provider.getUserAuthMethods(userId).isEmpty());
    assertTrue(provider.removeUserAuthMethod(userId, auth.type()));
    assertTrue(provider.getUserAuthMethods(userId).isEmpty());
  }

  @Test
  void getUserDataTest() throws InternalErrorException, BadDataException {

    assertNull(provider.getUserData(-1));

    UserData user = new UserData("userdata", null, LocalDateTime.now());
    int userId = provider.createUser(user);
    UserData userData = provider.getUserData(userId);
    assertNotNull(userData);
    assertEquals(user.username(), userData.username());
    assertEquals(user.picture(), userData.picture());
  }

  @Test
  void basicUserRolesTest() throws InternalErrorException, BadDataException {

    assertNull(provider.getUserRoles(-1));
    assertThrows(BadDataException.class, () -> provider.alterUserRole(-1, 1, true));

    UserData user = new UserData("roles", null, LocalDateTime.now());
    int userId = provider.createUser(user);
    assertTrue(provider.getUserRoles(userId).isEmpty());
    assertTrue(provider.alterUserRole(userId, 1, true));
    assertFalse(provider.getUserRoles(userId).isEmpty());
  }

  @Test
  void userModificationsTest() throws InternalErrorException, BadDataException {

    assertThrows(BadDataException.class, () -> provider.updateUsername(-1, ""));
    assertThrows(BadDataException.class, () -> provider.updateUserPicture(-1, new byte[] {}));
    assertThrows(BadDataException.class, () -> provider.deleteUser(-1));

    UserData user = new UserData("mod", null, LocalDateTime.now());
    int userId = provider.createUser(user);

    UserData tmp = new UserData("tmp", null, LocalDateTime.now());
    provider.createUser(tmp);
    assertThrows(BadDataException.class, () -> provider.updateUsername(userId, "tmp"));

    provider.updateUsername(userId, "mod1");
    assertEquals("mod1", provider.getUserData(userId).username());

    provider.updateUserPicture(userId, new byte[] {0});
    assertNotNull(provider.getUserData(userId).picture());
    assertArrayEquals(new byte[] {0}, provider.getUserData(userId).picture());

    provider.deleteUser(userId);
  }
}
