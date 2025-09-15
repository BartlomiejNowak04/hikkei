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
    if (data == null) throw new BadDataException(invalid_data);
    if (data.pageId() == null) throw new BadDataException(invalid_page);
    if (data.content() == null) throw new BadDataException(invalid_content);

    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO comments (page_id, user_id, content) VALUES (?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, data.pageId());
      st.setObject(2, data.userId());
      st.setString(3, data.content());
      st.executeUpdate();

      try (ResultSet rs = st.getGeneratedKeys()) {
        if (!rs.next()) throw new InternalErrorException(internal_comment);
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();

      if (message.contains("comments_page_id_fkey")) throw new BadDataException(invalid_page);
      if (message.contains("table pages is marked as deleted"))
        throw new BadDataException(invalid_page);

      if (message.contains("comments_user_id_fkey")) throw new BadDataException(invalid_user);
      if (message.contains("table users is marked as deleted"))
        throw new BadDataException(invalid_user);

      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public void deleteComment(int id) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE comments SET deleted=true, date_deleted=now() WHERE id = ?")) {
      st.setInt(1, id);
      st.executeUpdate();
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public CommentData getById(int id) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement("SELECT * FROM comments WHERE id = ?")) {
      st.setInt(1, id);

      try (ResultSet rs = st.executeQuery()) {
        if (!rs.next()) return null;
        return getCommentData(rs);
      }

    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
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
            "SELECT * FROM comments WHERE page_id = ? AND deleted = false ORDER BY date DESC")) {
      st.setInt(1, pageId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<CommentData> res = new ArrayList<>();
        while (rs.next()) {
          res.add(getCommentData(rs));
        }
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  @Override
  public List<CommentData> getByUser(Integer userId) throws InternalErrorException {
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
            "SELECT * FROM comments WHERE user_id = ? AND deleted = false ORDER BY date DESC")) {
      st.setInt(1, userId);
      try (ResultSet rs = st.executeQuery()) {
        ArrayList<CommentData> res = new ArrayList<>();
        while (rs.next()) {
          res.add(getCommentData(rs));
        }
        return res;
      }
    } catch (SQLException e) {
      throw new InternalErrorException(internal_error);
    }
  }

  private CommentData getCommentData(ResultSet rs) throws SQLException {
    if (rs.getBoolean("deleted")) return CommentData.DELETED;
    return new CommentData(
        rs.getInt("id"),
        rs.getInt("page_id"),
        rs.getInt("user_id"),
        rs.getString("content"),
        rs.getTimestamp("date").toLocalDateTime());
  }
}
