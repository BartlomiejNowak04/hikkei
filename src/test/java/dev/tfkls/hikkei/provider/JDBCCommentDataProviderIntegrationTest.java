package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.provider.JDBC.JDBCCommentDataProvider;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCCommentDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCCommentDataProvider provider;
  private int pageId;
  private int userId;

  @BeforeAll
  void init() throws SQLException {
    provider = new JDBCCommentDataProvider(connection);

    PreparedStatement stCat =
        connection.prepareStatement("INSERT INTO categories_with_data (name) VALUES (?)");
    stCat.setString(1, "test-category");
    stCat.executeUpdate();

    PreparedStatement stUsr =
        connection.prepareStatement("INSERT INTO users_with_data (username) VALUES (?)");
    stUsr.setString(1, "testname");
    stUsr.executeUpdate();

    PreparedStatement stPage =
        connection.prepareStatement(
            "INSERT INTO pages_with_data (title, category, user_id) VALUES (?, ?, ?)");
    stPage.setString(1, "test-page");
    stPage.setInt(2, 1);
    stPage.setInt(3, 1);
    stPage.executeUpdate();

    pageId = 1;
    userId = 1;
  }

  @Test
  void pageWithNoCommentsReturnsEmptyList() throws InternalErrorException, SQLException {
    PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO pages_with_data (title, category, user_id) VALUES (?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
    st.setString(1, "no-comments");
    st.setInt(2, 1);
    st.setInt(3, 1);
    st.executeUpdate();

    st.getGeneratedKeys().next();
    int noCommentsId = st.getGeneratedKeys().getInt(1);

    List<CommentData> comments = provider.getByPage(noCommentsId);
    assertNotNull(comments);
    assertTrue(comments.isEmpty());
  }

  @Test
  void userWithNoCommentsReturnsEmptyList() throws InternalErrorException, SQLException {
    PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO users_with_data (username) VALUES (?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
    st.setString(1, "no-comments");
    st.executeUpdate();

    st.getGeneratedKeys().next();
    userId = st.getGeneratedKeys().getInt(1);

    List<CommentData> comments = provider.getByUser(userId);
    assertNotNull(comments);
    assertTrue(comments.isEmpty());
  }

  @Test
  void addCommentSuccess() throws BadDataException, InternalErrorException {
    CommentData comment =
        new CommentData(-1, pageId, userId, "This is a test comment", LocalDateTime.now());
    int commentId = provider.addComment(comment);

    assertTrue(commentId > 0);
  }

  @Test
  void addCommentFailsWithInvalidPage() {
    CommentData comment =
        new CommentData(-1, -9999, userId, "Invalid page comment", LocalDateTime.now());
    assertThrows(BadDataException.class, () -> provider.addComment(comment));
  }

  @Test
  void deleteCommentSuccess() throws BadDataException, InternalErrorException {
    CommentData comment = new CommentData(-1, pageId, userId, "To be deleted", LocalDateTime.now());
    int commentId = provider.addComment(comment);

    assertDoesNotThrow(() -> provider.deleteComment(commentId));
  }

  @Test
  void deleteCommentWorksForInvalidId() {
    try {
      provider.deleteComment(-1);
    } catch (Exception e) {
      fail();
    }
  }
}
