package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.internal_error;
import static dev.tfkls.hikkei.provider.JDBC.Config.invalid_role;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.role.RoleData;
import dev.tfkls.hikkei.model.role.RoleDataProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JDBCRoleDataProvider implements RoleDataProvider {
  private final Connection connection;

  public JDBCRoleDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public int createRole(RoleData data) throws InternalErrorException, BadDataException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO roles_with_data (name) VALUES (?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setString(1, data.name());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(invalid_role);
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<RoleData> getRoles() throws InternalErrorException {
    return List.of();
  }

  @Override
  public List<RoleData> getByUserId(int userId) throws InternalErrorException {
    return List.of();
  }

  @Override
  public RoleData getById(int id) throws InternalErrorException {
    return null;
  }

  @Override
  public void changeName(int id, String name) throws InternalErrorException, BadDataException {}

  @Override
  public void deleteRole(int id) throws InternalErrorException {}
}
