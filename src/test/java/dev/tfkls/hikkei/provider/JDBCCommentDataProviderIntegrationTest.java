package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCCommentDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCCommentDataProvider provider;
  private int pageId;

  @BeforeAll
  void init() throws BadDataException, InternalErrorException {
    provider = new JDBCCommentDataProvider(connection);
    JDBCPageDataProvider pageProvider = new JDBCPageDataProvider(connection);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
      st.setString(1, "test-category");
      st.executeUpdate();
    } catch (SQLException ignored) {
    }

    pageId = pageProvider.createPage(new PageData(null, "test-page", 1, false));
  }

  @Test
  void addCommentSuccess() throws BadDataException, InternalErrorException {
    CommentData comment =
        new CommentData(-1, pageId, "This is a test comment", LocalDateTime.now(), false);
    Integer commentId = provider.addComment(comment);

    assertNotNull(commentId);
    assertTrue(commentId > 0);
  }

  @Test
  void addCommentFailsWithInvalidPage() {
    CommentData comment =
        new CommentData(-1, -9999, "Invalid page comment", LocalDateTime.now(), false);
    assertThrows(BadDataException.class, () -> provider.addComment(comment));
  }

  @Test
  void deleteCommentSuccess() throws BadDataException, InternalErrorException {
    CommentData comment = new CommentData(-1, pageId, "To be deleted", LocalDateTime.now(), false);
    int commentId = provider.addComment(comment);

    assertDoesNotThrow(() -> provider.deleteComment(pageId, commentId));
  }

  @Test
  void deleteCommentFailsForInvalidId() {
    assertThrows(BadDataException.class, () -> provider.deleteComment(pageId, -1));
  }
}
