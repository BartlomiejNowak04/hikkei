package dev.tfkls.hikkei.provider.JDBC;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JDBCCommentDataProvider implements CommentDataProvider {
  private final Connection connection;

  public JDBCCommentDataProvider(Connection connection) {
    this.connection = connection;
  }

  @Override
  public int addComment(CommentData data) throws BadDataException, InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO comments (page_id, content) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, data.pageId());
      st.setString(2, data.content());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(internal_comment);
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("comments_page_id_fkey")) throw new BadDataException(invalid_page);
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteComment(int id) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE comments SET deleted=true, date_deleted=now() WHERE id = ?")) {
      st.setInt(1, id);
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public CommentData getById(int id) throws InternalErrorException {
    return null;
  }

  @Override
  public List<CommentData> getByPage(int pageId) throws InternalErrorException {
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
            "SELECT * FROM comments WHERE page_id = ? ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        return getCommentDataFromResultSet(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<CommentData> getByUser(int userId) throws InternalErrorException {
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
            "SELECT * FROM comments WHERE user_id = ? ORDER BY date DESC")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        return getCommentDataFromResultSet(rs);
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  private List<CommentData> getCommentDataFromResultSet(ResultSet rs) throws SQLException {
    ArrayList<CommentData> res = new ArrayList<>();
    while (rs.next())
      res.add(
          new CommentData(
              rs.getInt("id"),
              rs.getInt("page_id"),
              rs.getInt("user_id"),
              rs.getString("content"),
              rs.getTimestamp("date").toLocalDateTime()));
    return res;
  }
}
