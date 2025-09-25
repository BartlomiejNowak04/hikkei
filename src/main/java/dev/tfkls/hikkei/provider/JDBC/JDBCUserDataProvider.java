package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.permission.PermissionLevel;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JDBCUserDataProvider implements UserDataProvider {
  private final Connection connection;

  public JDBCUserDataProvider(Connection conn) {
    connection = conn;
  }

  @Override
  public boolean doesUserExist(int userId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT 1 FROM users WHERE id = ? and deleted = false")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public UserData getUserByAuth(AuthData auth) throws InternalErrorException {
    if (auth == null) return null;
    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM auth JOIN users_with_data dt ON dt.id=auth.user_id WHERE type = ? AND token = ?")) {
      st.setString(1, auth.type());
      st.setString(2, auth.token());
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        else return getData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public UserData getUserData(int userId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM users_with_data WHERE id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        return getData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<AuthData> getUserAuthMethods(int userId) throws InternalErrorException {
    if (!doesUserExist(userId)) return null;

    try (PreparedStatement st =
        connection.prepareStatement("SELECT type, token FROM auth WHERE user_id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<AuthData> res = new ArrayList<>();
        while (rs.next()) res.add(new AuthData(rs.getString(1), rs.getString(2)));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public int createUser(UserData data) throws BadDataException, InternalErrorException {
    if (data == null) throw new BadDataException(invalid_data);
    if (data.username() == null) throw new BadDataException(invalid_username);
    // NOTE: decided not to allow join date other than default, nor to add deleted user accounts
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO users_with_data (username, picture, global_permission) VALUES (?, ?, ?::PermissionLevel)",
            Statement.RETURN_GENERATED_KEYS)) {
      st.setString(1, data.username());
      st.setBytes(2, data.picture());
      st.setString(3, data.globalPermission().name());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(internal_user);
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("already taken")) throw new BadDataException(taken_name);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updateUsername(int userId, String username)
      throws InternalErrorException, BadDataException {
    if (!doesUserExist(userId)) throw new BadDataException(invalid_user);
    if (username == null) throw new BadDataException(invalid_username);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO user_name (username,user_id) VALUES (?, ?)")) {
      st.setString(1, username);
      st.setInt(2, userId);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("already taken")) throw new BadDataException(taken_name);
      if (message.contains("table users is marked as deleted"))
        throw new BadDataException(invalid_user);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updateUserPicture(int userId, byte[] picture)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(invalid_user);
    if (picture == null) throw new BadDataException(invalid_datafile);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO user_picture (picture,user_id) VALUES (?, ?)")) {
      st.setBytes(1, picture);
      st.setInt(2, userId);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("table users is marked as deleted"))
        throw new BadDataException(invalid_user);
      throw new InternalErrorException(internal_error);
    }
  }

  // TODO: TEST THIS
  @Override
  public void updateGlobalPermission(int userId, PermissionLevel permissionLevel)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(invalid_user);
    if (permissionLevel == null) throw new BadDataException(invalid_permission);

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO user_permission (user_id, global_permission) VALUES (?, ?::PermissionLevel)")) {
      st.setInt(1, userId);
      st.setString(2, permissionLevel.name());
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteUser(int userId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE users SET deleted = ?, date_deleted=now() WHERE id = ?")) {
      st.setBoolean(1, true);
      st.setInt(2, userId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void addUserAuthMethod(int userId, AuthData auth)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(invalid_user);
    if (auth == null) throw new BadDataException(invalid_data);
    if (auth.token() == null) throw new BadDataException(invalid_token);
    if (auth.type() == null) throw new BadDataException(invalid_type);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO auth (user_id, type, token) VALUES (?, ?, ?)")) {
      st.setInt(1, userId);
      st.setString(2, auth.type());
      st.setString(3, auth.token());
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("table users is marked as deleted"))
        throw new BadDataException(invalid_user);
      if (message.contains("auth_pkey")) throw new BadDataException(taken_type);
      if (message.contains("auth_user_id_fkey")) throw new BadDataException(invalid_user);
      // This exception should be buried - it would give the information that the provided token is
      // valid for some account
      if (message.contains("auth_type_token_key")) return;
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void removeUserAuthMethod(int userId, String authType)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(invalid_user);
    if (authType == null) throw new BadDataException(invalid_type);

    try (PreparedStatement st =
        connection.prepareStatement("DELETE FROM auth WHERE user_id = ? AND type = ?")) {
      st.setInt(1, userId);
      st.setString(2, authType);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("table users is marked as deleted"))
        throw new BadDataException(invalid_user);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public Map<Integer, PermissionLevel> getCategoriesPermissions(int userId)
      throws InternalErrorException {
    if (!doesUserExist(userId)) return null;

    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM user_category_current WHERE user_id = ?")) {
      st.setInt(1, userId);
      ResultSet rs = st.executeQuery();
      HashMap<Integer, PermissionLevel> res = new HashMap<>();
      while (rs.next())
        res.put(rs.getInt("category_id"), PermissionLevel.valueOf(rs.getString("permission")));
      return res;
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  private UserData getData(ResultSet rs) throws SQLException {
    if (rs.getBoolean("deleted")) return UserData.DELETED;
    return new UserData(
        rs.getInt("id"),
        rs.getString("username"),
        rs.getBytes("picture"),
        rs.getTimestamp("joined").toLocalDateTime(),
        PermissionLevel.valueOf(rs.getString("global_permission")));
  }
}
