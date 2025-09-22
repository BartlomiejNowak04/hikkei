package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;

import dev.tfkls.hikkei.model.category.CategoryData;
import dev.tfkls.hikkei.model.category.CategoryDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class JDBCCategoryDataProvider implements CategoryDataProvider {
  private final Connection connection;

  public JDBCCategoryDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public int createCategory(CategoryData data) throws InternalErrorException, BadDataException {
    if (data == null) throw new BadDataException(invalid_data);
    if (data.name() == null) throw new BadDataException(invalid_name);

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
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM categories_with_data WHERE deleted = false")) {
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<CategoryData> res = new ArrayList<>();
        while (rs.next()) {
          res.add(getCategoryData(rs));
        }
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public CategoryData getById(int id) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM categories_with_data WHERE id = ?")) {
      st.setInt(1, id);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        return getCategoryData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void changeName(int id, String name) throws InternalErrorException, BadDataException {
    if (name == null) throw new BadDataException(invalid_name);

    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM categories WHERE id = ?")) {
      st.setInt(1, id);
      if (!st.executeQuery().next()) throw new BadDataException(invalid_category);
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO category_name(category_id, name) VALUES (?, ?)")) {
      st.setInt(1, id);
      st.setString(2, name);

      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  // TODO: TEST
  public void updateDefaultPermission(int id, PermissionLevel permissionLevel)
      throws InternalErrorException, BadDataException {
    if (permissionLevel == null) throw new BadDataException(invalid_permission);

    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE categories SET default_permission=?::PermissionLevel WHERE id = ?")) {
      st.setString(1, permissionLevel.name());
      st.setInt(2, id);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  // TODO: TEST
  @Override
  public Optional<PermissionLevel> getUserPermission(int categoryId, int userId)
      throws InternalErrorException, BadDataException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT permission FROM user_category_current WHERE user_id = ? AND category_id = ?")) {
      st.setInt(1, userId);
      st.setInt(2, categoryId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        String enumName = rs.getString("permission");
        if (enumName == null) return Optional.empty();
        else return Optional.of(PermissionLevel.valueOf(enumName));
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updateUserPermission(int categoryId, int userId, PermissionLevel level)
      throws InternalErrorException, BadDataException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM categories WHERE id = ?")) {
      st.setInt(1, categoryId);
      if (!st.executeQuery().next()) throw new InternalErrorException(invalid_category);
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
      st.setInt(1, userId);
      if (!st.executeQuery().next()) throw new InternalErrorException(invalid_user);
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO user_category(user_id, category_id, permission) VALUES (?, ?, ?::PermissionLevel)")) {
      st.setInt(1, userId);
      st.setInt(2, categoryId);
      st.setString(3, level == null ? null : level.toString());
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("table users is marked as deleted"))
        throw new BadDataException(invalid_user);
      if (message.contains("table categories is marked as deleted"))
        throw new BadDataException(invalid_category);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteCategory(int id) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE categories SET deleted=true, date_deleted=now() WHERE id = ?")) {
      st.setInt(1, id);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public Map<Integer, PermissionLevel> getUsersPermissions(int categoryId)
      throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM categories WHERE id = ?")) {
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

  private CategoryData getCategoryData(ResultSet rs) throws SQLException {
    if (rs.getBoolean("deleted")) return CategoryData.DELETED;
    return new CategoryData(
        rs.getInt("id"),
        rs.getString("name"),
        rs.getTimestamp("created").toLocalDateTime(),
        PermissionLevel.valueOf(rs.getString("default_permission")));
  }
}
