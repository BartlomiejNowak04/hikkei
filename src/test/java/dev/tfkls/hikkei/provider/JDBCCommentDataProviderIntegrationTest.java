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
  private int pageId2;
  private int userId2;

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

    PreparedStatement stUsr2 =
        connection.prepareStatement("INSERT INTO users_with_data (username) VALUES (?)");
    stUsr2.setString(1, "testname2");
    stUsr2.executeUpdate();

    PreparedStatement stPage =
        connection.prepareStatement(
            "INSERT INTO pages_with_data (title, category, user_id) VALUES (?, ?, ?)");
    stPage.setString(1, "test-page");
    stPage.setInt(2, 1);
    stPage.setInt(3, 1);
    stPage.executeUpdate();

    PreparedStatement stPage2 =
        connection.prepareStatement(
            "INSERT INTO pages_with_data (title, category, user_id) VALUES (?, ?, ?)");
    stPage2.setString(1, "test-page-2");
    stPage2.setInt(2, 1);
    stPage2.setInt(3, 1);
    stPage2.executeUpdate();

    pageId = 1;
    userId = 1;
    pageId2 = 2;
    userId2 = 2;
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
    CommentData comment = new CommentData(-1, pageId, userId, "test", LocalDateTime.now());
    int commentId = provider.addComment(comment);

    assertTrue(commentId > 0);
  }

  @Test
  void addCommentFailsWithInvalidPage() {
    CommentData comment = new CommentData(-1, -9999, userId, "invalid", LocalDateTime.now());
    assertThrows(BadDataException.class, () -> provider.addComment(comment));
  }

  @Test
  void deleteCommentSuccess() throws BadDataException, InternalErrorException {
    CommentData comment = new CommentData(-1, pageId, userId, "delete", LocalDateTime.now());
    int commentId = provider.addComment(comment);

    assertDoesNotThrow(() -> provider.deleteComment(commentId));
  }

  @Test
  void deleteCommentWorksForInvalidId() {
    assertDoesNotThrow(() -> provider.deleteComment(-1));
  }

  @Test
  void getByIdSuccess() throws BadDataException, InternalErrorException {
    CommentData comment = new CommentData(-1, pageId, userId, "get-by-id", LocalDateTime.now());
    int commentId = provider.addComment(comment);
    CommentData retrievedComment = provider.getById(commentId);
    assertNotNull(retrievedComment);
    assertEquals(comment.pageId(), retrievedComment.pageId());
    assertEquals(comment.userId(), retrievedComment.userId());
    assertEquals(commentId, retrievedComment.id());
    assertEquals(comment.content(), retrievedComment.content());
  }

  @Test
  void deleteCommentActuallyDeletes() throws BadDataException, InternalErrorException {
    CommentData comment = new CommentData(-1, pageId, userId, "delete-v2", LocalDateTime.now());
    int commentId = provider.addComment(comment);

    provider.deleteComment(commentId);
    CommentData retrievedComment = provider.getById(commentId);
    assertNotNull(retrievedComment);
    assertEquals(retrievedComment, CommentData.DELETED);
  }

  @Test
  void getByPageSuccess() throws BadDataException, InternalErrorException {
    CommentData comment = new CommentData(-1, pageId2, userId, "page", LocalDateTime.now());
    int commentId = provider.addComment(comment);
    CommentData comment2 = new CommentData(-1, pageId2, userId, "page2", LocalDateTime.now());
    int commentId2 = provider.addComment(comment2);

    List<CommentData> comments = provider.getByPage(pageId2);
    assertNotNull(comments);
    assertEquals(comments.size(), 2);
    assertEquals(commentId2, comments.getFirst().id());
    assertEquals(commentId, comments.getLast().id());
  }

  @Test
  void getByPageFailsWithInvalidPage() throws InternalErrorException {
    assertNull(provider.getByPage(-1));
  }

  @Test
  void getByUserSuccess() throws BadDataException, InternalErrorException {
    CommentData comment = new CommentData(-1, pageId, userId2, "user", LocalDateTime.now());
    int commentId = provider.addComment(comment);
    CommentData comment2 = new CommentData(-1, pageId, userId2, "user2", LocalDateTime.now());
    int commentId2 = provider.addComment(comment2);

    List<CommentData> comments = provider.getByUser(userId2);
    assertNotNull(comments);
    assertEquals(comments.size(), 2);
    assertEquals(commentId2, comments.getFirst().id());
    assertEquals(commentId, comments.getLast().id());
  }

  @Test
  void getByUserFailsWithInvalidUser() throws InternalErrorException {
    assertNull(provider.getByUser(-1));
  }
}
