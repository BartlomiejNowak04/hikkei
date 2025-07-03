package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.BadHandleException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCUserDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCUserDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCUserDataProvider(connection);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO roles (name) VALUES (?)")) {
      st.setString(1, "test");
      st.executeUpdate();
    } catch (SQLException ignored) {
    }
  }

  @Test
  void nonExistentUserDoesNotExist() {
    assertFalse(provider.doesUserExist(-1));
  }

  @Test
  void freshlyAddedUserExists() {
    UserData user = new UserData("testname", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);
    assertTrue(userId > 0);
    assertTrue(provider.doesUserExist(userId));
  }

  @Test
  void basicAuthTest() {
    UserData user = new UserData("auth", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);
    AuthData auth = new AuthData("google", "abc");
    assertTrue(provider.getUserByAuth(auth).isEmpty());
    assertThrows(BadHandleException.class, () -> provider.addUserAuthMethod(-1, auth));
    assertTrue(provider.addUserAuthMethod(userId, auth));
    assertTrue(provider.getUserByAuth(auth).isPresent());
  }

  @Test
  void getUserAuthMethodsTest() {

    assertThrows(BadHandleException.class, () -> provider.getUserAuthMethods(-1));
    assertThrows(BadHandleException.class, () -> provider.removeUserAuthMethod(-1, ""));

    UserData user = new UserData("authmethods", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);
    assertTrue(provider.getUserAuthMethods(userId).isEmpty());
    AuthData auth = new AuthData("google", "abcd");
    assertFalse(provider.removeUserAuthMethod(userId, auth.type()));
    provider.addUserAuthMethod(userId, auth);
    assertFalse(provider.getUserAuthMethods(userId).isEmpty());
    assertTrue(provider.removeUserAuthMethod(userId, auth.type()));
    assertTrue(provider.getUserAuthMethods(userId).isEmpty());
  }

  @Test
  void getUserDataTest() {

    assertThrows(BadHandleException.class, () -> provider.getUserData(-1));

    UserData user = new UserData("userdata", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);
    UserData userData = provider.getUserData(userId);
    assertNotNull(userData);
    assertEquals(user.username(), userData.username());
    assertEquals(user.picture(), userData.picture());
    assertFalse(userData.deleted());
  }

  @Test
  void basicUserRolesTest() {

    assertThrows(BadHandleException.class, () -> provider.getUserRoles(-1));
    assertThrows(BadHandleException.class, () -> provider.alterUserRole(-1, 1, true));

    UserData user = new UserData("roles", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);
    assertTrue(provider.getUserRoles(userId).isEmpty());
    assertFalse(provider.alterUserRole(userId, 1, false));
    assertTrue(provider.alterUserRole(userId, 1, true));
    assertFalse(provider.getUserRoles(userId).isEmpty());
  }

  @Test
  void userModificationsTest() {

    assertThrows(BadHandleException.class, () -> provider.updateUsername(-1, ""));
    assertThrows(BadHandleException.class, () -> provider.updateUserPicture(-1, new byte[] {}));
    assertThrows(BadHandleException.class, () -> provider.deleteUser(-1));

    UserData user = new UserData("mod", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);

    UserData tmp = new UserData("tmp", Optional.empty(), LocalDateTime.now(), false);
    provider.createUser(tmp);
    assertThrows(BadDataException.class, () -> provider.updateUsername(userId, "tmp"));

    provider.updateUsername(userId, "mod1");
    assertEquals("mod1", provider.getUserData(userId).username());

    provider.updateUserPicture(userId, new byte[] {0});
    assertTrue(provider.getUserData(userId).picture().isPresent());
    assertArrayEquals(new byte[] {0}, provider.getUserData(userId).picture().get());

    provider.deleteUser(userId);
    assertTrue(provider.getUserData(userId).deleted());
  }
}
