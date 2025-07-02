package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
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
  void testDoesUserExist_returnsTrue_whenUserFound() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.isBeforeFirst()).thenReturn(true);

    boolean exists = provider.doesUserExist(42);

    assertTrue(exists);
  }

  @Test
  void testDoesUserExist_returnsFalse_whenUserNotFound() throws SQLException {
    when(connection.prepareStatement(any())).thenReturn(statement);
    when(statement.executeQuery()).thenReturn(resultSet);
    when(resultSet.isBeforeFirst()).thenReturn(false);

    boolean exists = provider.doesUserExist(99);

    assertFalse(exists);
  }
}
