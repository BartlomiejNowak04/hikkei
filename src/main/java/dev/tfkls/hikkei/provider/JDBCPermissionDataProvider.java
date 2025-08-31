package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionDataProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCPermissionDataProvider implements PermissionDataProvider {
  private final Connection connection;

  public JDBCPermissionDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public int createRole(String role) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO roles (name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setString(1, role);
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException("Couldn't create a new role");
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new InternalErrorException("Database internal error");
    }
  }

  @Override
  public int createCategory(String category) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO categories (name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setString(1, category);
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException("Couldn't create a new category");
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new InternalErrorException("Database internal error");
    }
  }
}
