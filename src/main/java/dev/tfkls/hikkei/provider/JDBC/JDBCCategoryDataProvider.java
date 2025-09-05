package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.internal_category;
import static dev.tfkls.hikkei.provider.JDBC.Config.internal_error;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCCategoryDataProvider implements CategoryDataProvider {
  private final Connection connection;

  public JDBCCategoryDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public int createCategory(CategoryData data) throws InternalErrorException, BadDataException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO categories_with_data (name) VALUES (?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setString(1, data.name());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(internal_category);
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<CategoryData> getCategories() throws InternalErrorException {
    return List.of();
  }

  @Override
  public CategoryData getById(int id) throws InternalErrorException {
    return null;
  }

  @Override
  public void changeName(int id, String name) throws InternalErrorException, BadDataException {}

  @Override
  public void updatePermission(int categoryId, int userId, PermissionLevel level)
      throws InternalErrorException, BadDataException {}

  @Override
  public void deleteCategory(int id) throws InternalErrorException {}

  @Override
  public Map<Integer, PermissionLevel> getUsersPermissions(int categoryId)
      throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT 1 FROM categories WHERE id = ?")) {
      st.setInt(1, categoryId);
      if (!st.executeQuery().next()) return null;
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM user_category_current WHERE category_id = ?")) {
      st.setInt(1, categoryId);
      ResultSet rs = st.executeQuery();
      HashMap<Integer, PermissionLevel> res = new HashMap<>();
      while (rs.next())
        res.put(rs.getInt("user_id"), PermissionLevel.valueOf(rs.getString("permission")));
      return res;
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }
}
