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
}
