package dev.tfkls.hikkei.provider.nullability;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.provider.JDBC.Config;
import dev.tfkls.hikkei.provider.JDBC.JDBCUserDataProvider;
import dev.tfkls.hikkei.provider.JDBCIntegrationTest;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCUserDataProviderNullIntegrationTest extends JDBCIntegrationTest {
  private JDBCUserDataProvider provider;

  @BeforeAll
  void initProvider() throws SQLException {
    provider = new JDBCUserDataProvider(connection);

    create_user("first", null);
  }

  @Test
  void getUserByAuth_nullAuth_returnsNull() throws Exception {
    assertNull(provider.getUserByAuth(null));
  }

  @Test
  void getUserByAuth_nullType_returnsNull() throws Exception {
    AuthData auth = new AuthData(null, "validToken");
    assertNull(provider.getUserByAuth(auth));
  }

  @Test
  void getUserByAuth_nullToken_returnsNull() throws Exception {
    AuthData auth = new AuthData("password", null);
    assertNull(provider.getUserByAuth(auth));
  }

  @Test
  void createUser_nullUsername_throwsBadDataException() {
    UserData user = new UserData(null, new byte[] {1, 2, 3});
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createUser(user));
    assertEquals(Config.invalid_username, ex.getMessage());
  }

  @Test
  void createUser_nullData_throwsBadDataException() {
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createUser(null));
    assertEquals(Config.invalid_data, ex.getMessage());
  }

  @Test
  void updateUsername_nullUsername_throwsBadDataException() {
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.updateUsername(1, null));
    assertEquals(Config.invalid_username, ex.getMessage());
  }

  @Test
  void updateUserPicture_nullPicture_throwsBadDataException() {
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.updateUserPicture(1, null));
    assertEquals(Config.invalid_datafile, ex.getMessage());
  }

  @Test
  void addUserAuthMethod_nullType_throwsBadDataException() {
    AuthData auth = new AuthData(null, "token");
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.addUserAuthMethod(1, auth));
    assertEquals(Config.invalid_type, ex.getMessage());
  }

  @Test
  void addUserAuthMethod_nullToken_throwsBadDataException() {
    AuthData auth = new AuthData("password", null);
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.addUserAuthMethod(1, auth));
    assertEquals(Config.invalid_token, ex.getMessage());
  }

  @Test
  void addUserAuthMethod_nullData_throwsBadDataException() {
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.addUserAuthMethod(1, null));
    assertEquals(Config.invalid_data, ex.getMessage());
  }

  @Test
  void removeUserAuthMethod_nullType_throwsBadDataException() {
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.removeUserAuthMethod(1, null));
    assertEquals(Config.invalid_type, ex.getMessage());
  }
}
