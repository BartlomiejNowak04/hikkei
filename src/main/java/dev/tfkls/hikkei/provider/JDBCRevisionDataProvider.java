package dev.tfkls.hikkei.provider;

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
  public Integer createRevision(RevisionData data) {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO revisions (page_id, filename) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, data.pageId());
      st.setObject(2, data.filename());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) return null;
        return rs.getInt(1);
      }
    } catch (SQLException ignored) {
    }

    return null;
  }

  @Override
  public Integer getPage(int revId) {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT page_id FROM revisions WHERE id = ?")) {
      st.setInt(1, revId);
      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getInt(1);
      }
    } catch (SQLException ignored) {
    }

    return null;
  }

  @Override
  public boolean changeState(int revId, Boolean state) {
    try (PreparedStatement st =
        connection.prepareStatement("UPDATE revisions SET approved = ? WHERE id = ?")) {
      st.setBoolean(1, state);
      st.setInt(2, revId);
      return st.executeUpdate() > 0;
    } catch (SQLException ignored) {
    }

    return false;
  }
}
