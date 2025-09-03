package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
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
        connection.prepareStatement("INSERT INTO categories (name) VALUES (?)");
    stCat.setString(1, "test-category");
    stCat.executeUpdate();

    PreparedStatement stPage =
        connection.prepareStatement(
            "INSERT INTO pages (title, category) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
    stPage.setString(1, "test-page");
    stPage.setInt(2, 1);
    stPage.executeUpdate();

    stPage.getGeneratedKeys().next();
    pageId = stPage.getGeneratedKeys().getInt(1);

    PreparedStatement stUser =
        connection.prepareStatement(
            "INSERT INTO users (username) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
    stUser.setString(1, "testname");
    stUser.executeUpdate();

    stUser.getGeneratedKeys().next();
    userId = stUser.getGeneratedKeys().getInt(1);
  }

  @Test
  void pageWithNoCommentsReturnsEmptyList() throws InternalErrorException, SQLException {
    PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO pages (title, category) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
    st.setString(1, "no-comments");
    st.setInt(2, 1);
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
            "INSERT INTO users (username) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS);
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
