package dev.tfkls.hikkei.provider.internal;

import static dev.tfkls.hikkei.provider.JDBC.Config.internal_error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.provider.JDBC.JDBCUserDataProvider;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDBCUserDataProviderInternalErrorTest {
  private JDBCUserDataProvider provider;

  @BeforeAll
  void setUp() {
    Connection connection =
        mock(
            Connection.class,
            invocation -> {
              throw new SQLException("test");
            });
    provider = new JDBCUserDataProvider(connection);
  }

  @Test
  void doesUserExistThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.doesUserExist(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getUserByAuthThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(
            InternalErrorException.class,
            () -> provider.getUserByAuth(new AuthData("type", "token")));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getUserDataThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getUserData(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getUserAuthMethodsThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getUserAuthMethods(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void createUserThrowsInternalError() {
    UserData user = new UserData(1, "test", new byte[] {}, null, null);
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.createUser(user));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void updateUsernameThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.updateUsername(1, "name"));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void updateUserPictureThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(
            InternalErrorException.class, () -> provider.updateUserPicture(1, new byte[] {}));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void deleteUserThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.deleteUser(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void addUserAuthMethodThrowsInternalError() {
    AuthData auth = new AuthData("type", "token");
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.addUserAuthMethod(1, auth));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void removeUserAuthMethodThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.removeUserAuthMethod(1, "type"));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getCategoriesPermissionsThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getCategoriesPermissions(1));
    assertEquals(internal_error, ex.getMessage());
  }
}
