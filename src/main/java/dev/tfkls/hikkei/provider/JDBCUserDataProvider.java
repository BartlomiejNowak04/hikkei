package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JDBCUserDataProvider implements UserDataProvider {
  private final Connection connection;

  public JDBCUserDataProvider(Connection conn) {
    connection = conn;
  }

  @Override
  public boolean doesUserExist(int userId) {

    try (PreparedStatement st =
        connection.prepareStatement("SELECT COUNT(*) FROM users WHERE id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        return rs.isBeforeFirst();
      }
    } catch (SQLException ignored) {
    }

    return false;
  }

  @Override
  public Optional<Integer> getUserByAuth(AuthData auth) {

    try (PreparedStatement st =
        connection.prepareStatement("SELECT user_id FROM auth WHERE type = ? AND token = ?")) {
      st.setString(1, auth.type());
      st.setString(2, auth.token());
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return Optional.empty();
        else return Optional.of(rs.getInt(1));
      }
    } catch (SQLException ignored) {
    }

    return Optional.empty();
  }

  @Override
  public UserData getUserData(int userId) throws BadHandleException {

    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) throw new BadHandleException("Incorrect user handle");
        return new UserData(
            rs.getString(2),
            Optional.of(rs.getBytes(3)),
            rs.getTimestamp(4).toLocalDateTime(),
            rs.getBoolean(5));
      }
    } catch (SQLException ignored) {
    }

    return null;
  }

  @Override
  public List<Integer> getUserRoles(int userId) throws BadHandleException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("SELECT role_id FROM user_role WHERE user_id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<Integer> res = new ArrayList<>();
        while (rs.next()) res.add(rs.getInt(1));
        return res;
      }
    } catch (SQLException ignored) {
    }

    return new ArrayList<>();
  }

  @Override
  public List<AuthData> getUserAuthMethods(int userId) throws BadHandleException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("SELECT type, token FROM auth WHERE user_id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<AuthData> res = new ArrayList<>();
        while (rs.next()) res.add(new AuthData(rs.getString(1), rs.getString(2)));
        return res;
      }
    } catch (SQLException ignored) {
    }

    return new ArrayList<>();
  }

  @Override
  public Integer createUser(UserData data) throws BadDataException {
    // NOTE: decided not to allow join date other than default, nor to add deleted user accounts
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO users (username, picture) VALUES (? ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      st.setString(1, data.username());
      st.setBytes(2, data.picture().orElse(null));
      int num_rows = st.executeUpdate();
      if (num_rows == 1) throw new BadDataException("Couldn't create new user " + data.username());

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) return 0;
        return rs.getInt(1);
      }
    } catch (SQLException ignored) {
    }

    return 0;
  }

  @Override
  public void updateUsername(int userId, String username)
      throws BadHandleException, BadDataException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE users SET username = ? WHERE id = ?")) {
      st.setString(1, username);
      st.setInt(2, userId);
      int num_rows = st.executeUpdate();
      if (num_rows == 1) throw new BadDataException("Couldn't update username " + userId);
    } catch (SQLException ignored) {
    }
  }

  @Override
  public void updateUserPicture(int userId, byte[] picture) throws BadHandleException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE users SET picture = ? WHERE id = ?")) {
      st.setBytes(1, picture);
      st.setInt(2, userId);
      int num_rows = st.executeUpdate();
    } catch (SQLException ignored) {
    }
  }

  @Override
  public void deleteUser(int userId) throws BadHandleException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("UPDATE users SET deleted = ? WHERE id = ?")) {
      st.setBoolean(1, true);
      st.setInt(2, userId);
      st.executeUpdate();
    } catch (SQLException ignored) {
    }
  }

  @Override
  public boolean addUserAuthMethod(int userId, AuthData auth) throws BadHandleException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO auth (user_id, type, token) VALUES (? ? ?)")) {
      st.setInt(1, userId);
      st.setString(2, auth.type());
      st.setString(3, auth.token());
      return st.executeUpdate() > 0;
    } catch (SQLException ignored) {
    }
    return false;
  }

  @Override
  public boolean removeUserAuthMethod(int userId, String authType) throws BadHandleException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    try (PreparedStatement st =
        connection.prepareStatement("DELETE FROM auth WHERE user_id = ? AND type = ?")) {
      st.setInt(1, userId);
      st.setString(2, authType);
      return st.executeUpdate() > 0;
    } catch (SQLException ignored) {
    }
    return false;
  }

  @Override
  public boolean alterUserRole(int userId, int roleId, boolean value) throws BadHandleException {

    if (!doesUserExist(userId)) throw new BadHandleException("Incorrect user handle");

    if (value) {
      try (PreparedStatement st =
          connection.prepareStatement("INSERT INTO user_role (user_id, role_id) VALUES (? ?)")) {
        st.setInt(1, userId);
        st.setInt(2, roleId);
        return st.executeUpdate() > 0;
      } catch (SQLException ignored) {
      }
    } else {
      try (PreparedStatement st =
          connection.prepareStatement("DELETE FROM user_role WHERE user_id = ? AND role_id = ?")) {
        st.setInt(1, userId);
        st.setInt(2, roleId);
        return st.executeUpdate() > 0;
      } catch (SQLException ignored) {
      }
    }
    return false;
  }
}
