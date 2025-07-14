package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCRevisionDataProvider implements RevisionDataProvider {
  private final Connection connection;

  public JDBCRevisionDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public Integer createRevision(RevisionData data) throws BadDataException, InternalErrorException {
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
      throw new InternalErrorException("Database internal error");
    }
  }

  @Override
  public void changeState(int revId, int pageId, Boolean state)
      throws BadDataException, InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE revisions SET approved = ? WHERE id = ? AND page_id = ?")) {
      st.setObject(1, state);
      st.setInt(2, revId);
      if (st.executeUpdate() < 1) throw new BadDataException("Invalid revision id");
    } catch (SQLException e) {
      throw new InternalErrorException("Database internal error");
    }
  }
}
