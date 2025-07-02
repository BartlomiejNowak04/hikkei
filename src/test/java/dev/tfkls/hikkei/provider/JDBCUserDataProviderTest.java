package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import dev.tfkls.hikkei.model.exceptions.BadHandleException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JDBCUserDataProviderTest {
  private Connection connection;
  private PreparedStatement statement;
  private ResultSet resultSet;
  private JDBCUserDataProvider provider;

  @BeforeEach
  void setUp() {
    connection = mock(Connection.class);
    statement = mock(PreparedStatement.class);
    resultSet = mock(ResultSet.class);
    provider = new JDBCUserDataProvider(connection);
  }

  @Test
  void mockUserExistsWhenExists() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getInt(1)).thenReturn(1);

    boolean exists = provider.doesUserExist(42);

    assertTrue(exists);
  }

  @Test
  void mockUserNoExistWhenNoExists() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getInt(1)).thenReturn(0);

    boolean exists = provider.doesUserExist(99);

    assertFalse(exists);
  }

  @Test
  void mockGetUserByAuth() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getInt(1)).thenReturn(1);

    Optional<Integer> userId = provider.getUserByAuth(new AuthData("", ""));
    assertTrue(userId.isPresent() && userId.get() == 1);
  }

  @Test
  void mockGetUserByAuthFail() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    Optional<Integer> userId = provider.getUserByAuth(new AuthData("", ""));
    assertTrue(userId.isEmpty());
  }

  @Test
  void mockGetUserData() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getString(2)).thenReturn("test");
    when(resultSet.getBytes(3)).thenReturn(new byte[] {1, 2, 3});
    when(resultSet.getTimestamp(4)).thenReturn(new Timestamp(System.currentTimeMillis()));
    when(resultSet.getBoolean(5)).thenReturn(false);

    UserData data = provider.getUserData(1);
    assertSame("test", data.username());
    assertTrue(data.picture().isPresent());
    assertArrayEquals(data.picture().get(), new byte[] {1, 2, 3});
    assertTrue(data.joinedAt().isBefore(LocalDateTime.now()));
    assertFalse(data.deleted());
  }

  @Test
  void mockGetUserDataFail() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(false);

    assertThrows(BadHandleException.class, () -> provider.getUserData(1));
  }

  @Test
  void mockGetUserRoles() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(resultSet.getInt(1)).thenReturn(1).thenReturn(5);

    List<Integer> roles = provider.getUserRoles(1);
    assertEquals(1, roles.size());
    assertEquals(5, roles.getFirst());
  }

  @Test
  void mockGetUserRolesFail() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getInt(1)).thenReturn(0);

    assertThrows(BadHandleException.class, () -> provider.getUserRoles(1));
  }

  @Test
  void mockGetUserAuthMethods() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
    when(resultSet.getInt(1)).thenReturn(1);
    when(resultSet.getString(1)).thenReturn("test1");
    when(resultSet.getString(2)).thenReturn("test2");

    List<AuthData> auth = provider.getUserAuthMethods(1);
    assertEquals(1, auth.size());
    assertEquals("test1", auth.getFirst().type());
    assertEquals("test2", auth.getFirst().token());
  }

  @Test
  void mockGetUserAuthMethodsFail() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.next()).thenReturn(true);
    when(resultSet.getInt(1)).thenReturn(0);

    assertThrows(BadHandleException.class, () -> provider.getUserAuthMethods(1));
  }
}
