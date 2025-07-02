package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;

public class JDBCUserDataProvider implements UserDataProvider {
  private Connection connection;

  public JDBCUserDataProvider(Connection conn) {
    connection = conn;
  }

  @Override
  public boolean doesUserExist(int userId) {

    try (PreparedStatement st =
        connection.prepareStatement("SELECT COUNT(*) FROM users WHERE id = ?"); ) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery(); ) {
        return rs.isBeforeFirst();
      }
    } catch (SQLException e) {
    }
    ;

    return false;
  }

  @Override
  public Optional<Integer> getUserByAuth(AuthData auth) {

    try (PreparedStatement st =
        connection.prepareStatement("SELECT user_id FROM auth WHERE type = ? AND token = ?"); ) {
      st.setString(1, auth.type());
      st.setString(2, auth.token());
      try (ResultSet rs = st.executeQuery(); ) {
        if (!rs.next()) return Optional.empty();
        else return Optional.of(rs.getInt(1));
      }
    } catch (SQLException e) {
    }

    return Optional.empty();
  }

  @Override
  public UserData getUserData(int userId) throws BadHandleException {

    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM users WHERE id = ?"); ) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery(); ) {
        if (!rs.next()) throw new BadHandleException("Incorrect user handle");
        return new UserData(
            rs.getString(2), rs.getTimestamp(4).toLocalDateTime(), rs.getBoolean(5));
      }
    } catch (SQLException e) {
    }

    return null;
  }

  @Override
  public Stream<Integer> getUserRoles(int userId) throws BadHandleException {

    try (PreparedStatement st =
        connection.prepareStatement("SELECT role_id FROM user_role WHERE user_id = ?"); ) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery(); ) {
        if (!rs.isBeforeFirst()) throw new BadHandleException("Incorrect user handle");
        ArrayList<Integer> res = new ArrayList<>();
        while (rs.next()) res.add(rs.getInt(1));
        return res.stream();
      }
    } catch (SQLException e) {
    }

    return Stream.empty();
  }

  @Override
  public Stream<AuthData> getUserAuthMethods(int userId) throws BadHandleException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT type, token FROM auth WHERE user_id = ?"); ) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery(); ) {
        if (!rs.isBeforeFirst()) throw new BadHandleException("Incorrect user handle");
        ArrayList<AuthData> res = new ArrayList<>();
        // TODO: type can be null in the DB but is not optional in AuthData. This should be made
        // consistent
        while (rs.next()) res.add(new AuthData(rs.getString(1), rs.getString(2)));
        return res.stream();
      }
    } catch (SQLException e) {
    }

    return Stream.empty();
  }

  @Override
  public Integer createUser(UserData data) throws BadDataException {
    // TODO: UserData has non-optional values that can be defaulted by the DB and doesn't have a
    // field for the profile picture, this should be changed
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO users (username, joined, deleted) VALUES (? ? ?)",
            Statement.RETURN_GENERATED_KEYS); ) {
      st.setString(1, data.username());
      st.setTimestamp(2, Timestamp.valueOf(data.joinedAt()));
      st.setBoolean(3, data.deleted());
      int num_rows = st.executeUpdate();
      if (num_rows == 1) throw new BadDataException("Couldn't create new user " + data.username());

      try (ResultSet rs = st.getGeneratedKeys(); ) {
        if (!rs.next()) return 0;
        return rs.getInt(1);
      }
    } catch (SQLException e) {
    }

    return 0;
  }

  @Override
  public void updateUser(int userId, UserData data) throws BadHandleException, BadDataException {
    // TODO: there should be an option to leave some data unspecified (so it doesn't change).
    // NOTE: Do we need a new class for that (e.g. UserBuilder, UserUpdater)?
    try (PreparedStatement st =
        connection.prepareStatement("UPDATE users SET username = ?, deleted = ? WHERE id = ?"); ) {
      st.setString(1, data.username());
      st.setBoolean(2, data.deleted());
      st.setInt(3, userId);
      int num_rows = st.executeUpdate();
      if (num_rows == 1) throw new BadHandleException("Couldn't update user " + userId);
    } catch (SQLException e) {
    }
  }

  @Override
  public void deleteUser(int userId) throws BadHandleException {
    try (PreparedStatement st =
        connection.prepareStatement("UPDATE users SET deleted = ? WHERE id = ?"); ) {
      st.setBoolean(1, true);
      st.setInt(2, userId);
      int num_rows = st.executeUpdate();
      if (num_rows == 1) throw new BadHandleException("Couldn't delete user " + userId);
    } catch (SQLException e) {
    }
  }

  @Override
  public boolean addUserAuthMethod(int userId, AuthData auth) throws BadHandleException {
    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO auth (user_id, type, token) VALUES (? ? ?)"); ) {
      st.setInt(1, userId);
      st.setString(2, auth.type());
      st.setString(3, auth.token());
      // TODO: we should differentiate between incorrect user id and other reasons for failure
      return st.executeUpdate() > 0;
    } catch (SQLException e) {
    }
    return false;
  }

  @Override
  public boolean removeUserAuthMethod(int userId, String authType) throws BadHandleException {
    try (PreparedStatement st =
        connection.prepareStatement("DELETE FROM auth WHERE user_id = ? AND type = ?"); ) {
      st.setInt(1, userId);
      st.setString(2, authType);
      // TODO: we should differentiate between incorrect user id and other reasons for failure
      return st.executeUpdate() > 0;
    } catch (SQLException e) {
    }
    return false;
  }

  @Override
  public boolean alterUserRole(int userId, int roleId, boolean value) throws BadHandleException {
    if (value) {
      try (PreparedStatement st =
          connection.prepareStatement("INSERT INTO user_role (user_id, role_id) VALUES (? ?)"); ) {
        st.setInt(1, userId);
        st.setInt(2, roleId);
        return st.executeUpdate() > 0;
      } catch (SQLException e) {
      }
    } else {
      try (PreparedStatement st =
          connection.prepareStatement(
              "DELETE FROM user_role WHERE user_id = ? AND role_id = ?"); ) {
        st.setInt(1, userId);
        st.setInt(2, roleId);
        return st.executeUpdate() > 0;
      } catch (SQLException e) {
      }
    }
    return false;
  }
}
