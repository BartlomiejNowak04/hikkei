package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCRevisionDataProvider implements RevisionDataProvider {
  private final Connection connection;

  public JDBCRevisionDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public int createRevision(RevisionData data) throws BadDataException, InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO revisions_with_data (page_id, user_id, file) VALUES (?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, data.pageId());
      st.setInt(2, data.userId());
      st.setObject(3, data.file());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(internal_revision);
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("revisions_page_id_fkey")) throw new BadDataException(invalid_page);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void changeState(int id, boolean state) throws BadDataException, InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO revision_approved (approved, revision_id) values (?, ?)")) {
      st.setBoolean(1, state);
      st.setInt(2, id);
      if (st.executeUpdate() < 1) throw new BadDataException(invalid_revision);
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("revision_approved_revision_id_fkey"))
        throw new BadDataException(invalid_revision);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<RevisionData> getByPage(int pageId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT COUNT(*) FROM pages WHERE id = ?")) {
      st.setInt(1, pageId);
      ResultSet rs = st.executeQuery();
      rs.next();
      if (rs.getInt(1) == 0) return null;
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM revisions_with_data WHERE page_id = ? ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<RevisionData> res = new ArrayList<>();
        while (rs.next()) res.add(getRevisionData(rs));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<RevisionData> getByUser(int userId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT COUNT(*) FROM users WHERE id = ?")) {
      st.setInt(1, userId);
      ResultSet rs = st.executeQuery();
      rs.next();
      if (rs.getInt(1) == 0) return null;
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st =
        connection.prepareStatement(
            "SELECT * FROM revisions_with_data WHERE user_id = ? ORDER BY date DESC")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<RevisionData> res = new ArrayList<>();
        while (rs.next()) res.add(getRevisionData(rs));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public RevisionData getById(int id) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM revisions_with_data WHERE id = ?")) {
      st.setInt(1, id);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) throw new InternalErrorException(invalid_revision);
        return getRevisionData(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteRevision(int id) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE revisions SET deleted=true, date_deleted=now() WHERE id = ?")) {
      st.setInt(1, id);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public byte[] getDraft(int userId, int pageId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM drafts WHERE user_id = ? AND page_id = ?")) {
      st.setInt(1, userId);
      st.setInt(2, pageId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getBytes("file");
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void saveDraft(int userId, int pageId, byte[] file)
      throws InternalErrorException, BadDataException {
    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM users WHERE id = ?")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) throw new BadDataException(invalid_user);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st = connection.prepareStatement("SELECT * FROM pages WHERE id = ?")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) throw new BadDataException(invalid_page);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO drafts(user_id, page_id, file) VALUES (?, ?, ?)"
                + "ON CONFLICT DO UPDATE SET file = EXCLUDED.file")) {
      st.setInt(1, userId);
      st.setInt(2, pageId);
      st.setBytes(3, file);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteDraft(int userId, int pageId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("DELETE FROM drafts WHERE user_id = ? AND page_id = ?")) {
      st.setInt(1, userId);
      st.setInt(2, pageId);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  private RevisionData getRevisionData(ResultSet rs) throws SQLException {
    return new RevisionData(
        rs.getInt("id"),
        rs.getInt("page_id"),
        rs.getInt("user_id"),
        rs.getBytes("file"),
        rs.getTimestamp("date").toLocalDateTime(),
        rs.getObject("approved", Boolean.class));
  }
}
