package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.user.AuthData;
import dev.tfkls.hikkei.model.user.UserData;
import dev.tfkls.hikkei.model.user.UserDataProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JDBCUserDataProvider implements UserDataProvider {
  private final Connection connection;
  private final String internal_error = "Database internal error";
  private final String name_taken = "Provided username is taken";
  private final String user_id = "Invalid user id";

  public JDBCUserDataProvider(Connection conn) {
    connection = conn;
  }

  @Override
  public boolean doesUserExist(int userId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT COUNT(*) FROM users WHERE id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return false;
        return rs.getInt(1) > 0;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public Integer getUserByAuth(AuthData auth) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT user_id FROM auth WHERE type = ? AND token = ?")) {
      st.setString(1, auth.type());
      st.setString(2, auth.token());
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        else return rs.getInt(1);
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
        return new UserData(
            rs.getString("username"),
            rs.getBytes("picture"),
            rs.getTimestamp("joined").toLocalDateTime());
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<Integer> getUserRoles(int userId) throws InternalErrorException {
    if (!doesUserExist(userId)) return null;

    try (PreparedStatement st =
        connection.prepareStatement("SELECT role_id FROM user_role_current WHERE user_id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<Integer> res = new ArrayList<>();
        while (rs.next()) res.add(rs.getInt(1));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<UserData> getByRole(int roleId) throws InternalErrorException {
    return List.of();
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
    // NOTE: decided not to allow join date other than default, nor to add deleted user accounts
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO users_with_data (username, picture) VALUES (?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      st.setString(1, data.username());
      st.setBytes(2, data.picture());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException("Couldn't create a new user");
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("already taken")) throw new BadDataException(name_taken);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updateUsername(int userId, String username)
      throws InternalErrorException, BadDataException {
    if (!doesUserExist(userId)) throw new BadDataException(user_id);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO user_name (username,user_id) VALUES (?, ?)")) {
      st.setString(1, username);
      st.setInt(2, userId);
      st.executeUpdate();
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("already taken")) throw new BadDataException(name_taken);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void updateUserPicture(int userId, byte[] picture)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(user_id);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO user_picture (picture,user_id) VALUES (?, ?)")) {
      st.setBytes(1, picture);
      st.setInt(2, userId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteUser(int userId) throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(user_id);

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
  public boolean addUserAuthMethod(int userId, AuthData auth)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(user_id);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO auth (user_id, type, token) VALUES (?, ?, ?)")) {
      st.setInt(1, userId);
      st.setString(2, auth.type());
      st.setString(3, auth.token());
      return st.executeUpdate() > 0;
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("auth_pkey"))
        throw new BadDataException("This authentication type is already defined");
      if (message.contains("auth_user_id_fkey")) throw new BadDataException(user_id);
      // This exception should be buried - it would give the information that the provided token is
      // valid for some account
      // if (message.contains("auth_type_token_key")) throw new BadDataException("This
      // authentication type is already defined");
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public boolean removeUserAuthMethod(int userId, String authType)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(user_id);

    try (PreparedStatement st =
        connection.prepareStatement("DELETE FROM auth WHERE user_id = ? AND type = ?")) {
      st.setInt(1, userId);
      st.setString(2, authType);
      return st.executeUpdate() > 0;
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public boolean alterUserRole(int userId, int roleId, boolean value)
      throws BadDataException, InternalErrorException {
    if (!doesUserExist(userId)) throw new BadDataException(user_id);

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO user_role (user_id, role_id, set) VALUES (?, ?, ?)")) {
      st.setInt(1, userId);
      st.setInt(2, roleId);
      st.setBoolean(3, value);
      return st.executeUpdate() > 0;
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("user_role_role_id_fkey")) throw new BadDataException("Invalid role id");
      if (message.contains("user_role_user_id_fkey")) throw new BadDataException(user_id);
      throw new InternalErrorException(internal_error);
    }
  }
}
