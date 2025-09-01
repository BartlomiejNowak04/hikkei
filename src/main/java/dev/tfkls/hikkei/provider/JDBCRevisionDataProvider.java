package dev.tfkls.hikkei.provider;

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
import java.util.UUID;

public class JDBCRevisionDataProvider implements RevisionDataProvider {
  private final Connection connection;
  private final String internal_error = "Database internal error";

  public JDBCRevisionDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public int createRevision(RevisionData data) throws BadDataException, InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO revisions (page_id, filename) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, data.pageId());
      st.setObject(2, data.filename());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException("Couldn't create a new revision");
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("revisions_page_id_fkey")) throw new BadDataException("Invalid page id");
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void changeState(int revId, int pageId, boolean state)
      throws BadDataException, InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE revisions SET approved = ? WHERE id = ? AND page_id = ?")) {
      st.setObject(1, state);
      st.setInt(2, revId);
      st.setInt(3, pageId);
      if (st.executeUpdate() < 1) throw new BadDataException("Invalid revision id");
    } catch (SQLException e) {
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
            "SELECT * FROM revisions WHERE page_id = ? ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<RevisionData> res = new ArrayList<>();
        while (rs.next())
          res.add(
              new RevisionData(
                  rs.getInt(1),
                  rs.getInt(2),
                  rs.getObject(3, UUID.class),
                  rs.getTimestamp(4).toLocalDateTime(),
                  rs.getObject(5, Boolean.class)));
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<RevisionData> getByUser(int userId) throws InternalErrorException {
    return List.of();
  }

  @Override
  public RevisionData getById(int id) throws InternalErrorException {
    return null;
  }

  @Override
  public void saveDraft(int userId, int pageId, UUID file)
      throws InternalErrorException, BadDataException {}

  @Override
  public UUID deleteDraft(int userId, int pageId) throws InternalErrorException, BadDataException {
    return null;
  }
}
