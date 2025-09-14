package dev.tfkls.hikkei.provider.internal;

import static dev.tfkls.hikkei.provider.JDBC.Config.internal_error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.provider.JDBC.JDBCCommentDataProvider;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JDBCCommentDataProviderInternalErrorTest {
  private JDBCCommentDataProvider provider;

  @BeforeAll
  void setUp() {
    Connection connection =
        mock(
            Connection.class,
            invocation -> {
              throw new SQLException("test");
            });
    provider = new JDBCCommentDataProvider(connection);
  }

  @Test
  void addCommentThrowsInternalError() {
    CommentData comment = new CommentData(1, 1, 1, "text", LocalDateTime.now());
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.addComment(comment));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void deleteCommentThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.deleteComment(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getByIdThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getById(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getByPageThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getByPage(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getByUserThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getByUser(1));
    assertEquals(internal_error, ex.getMessage());
  }
}
