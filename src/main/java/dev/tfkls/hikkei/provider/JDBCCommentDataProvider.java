package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
        if (!rs.next()) throw new InternalErrorException("Couldn't create a new comment");
        return rs.getInt(1);
      }
    } catch (SQLException e) {
      String message = e.getMessage();
      if (message.contains("comments_page_id_fkey")) throw new BadDataException("Invalid page id");
      throw new InternalErrorException("Database internal error");
    }
  }

  @Override
  public void deleteComment(int pageId, int commentId) throws InternalErrorException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE comments SET deleted = true WHERE id = ? AND page_id = ?")) {
      st.setInt(1, commentId);
      st.setInt(2, pageId);
    } catch (SQLException e) {
      throw new InternalErrorException("Database internal error");
    }
  }
}
