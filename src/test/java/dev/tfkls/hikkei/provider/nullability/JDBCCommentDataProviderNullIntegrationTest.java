package dev.tfkls.hikkei.provider.nullability;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.comment.CommentDataProvider;
import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.provider.JDBC.Config;
import dev.tfkls.hikkei.provider.JDBC.JDBCCommentDataProvider;
import dev.tfkls.hikkei.provider.JDBCIntegrationTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCCommentDataProviderNullIntegrationTest extends JDBCIntegrationTest {
  private CommentDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCCommentDataProvider(connection);
  }

  @Test
  void addComment_nullData_throwsBadDataException() {
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.addComment(null));
    assertEquals(Config.invalid_data, ex.getMessage());
  }

  @Test
  void addComment_nullPageId_throwsBadDataException() {
    CommentData data = new CommentData(0, null, 1, "content", LocalDateTime.now());
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.addComment(data));
    assertEquals(Config.invalid_page, ex.getMessage());
  }

  @Test
  void addComment_nullContent_throwsBadDataException() {
    CommentData data = new CommentData(0, 1, 1, null, LocalDateTime.now());
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.addComment(data));
    assertEquals(Config.invalid_content, ex.getMessage());
  }

  @Test
  void getById_notExisting_returnsNull() throws InternalErrorException {
    assertNull(provider.getById(-1));
  }

  @Test
  void getByPage_notExistingPage_returnsNull() throws InternalErrorException {
    assertNull(provider.getByPage(-1));
  }

  @Test
  void getByUser_notExistingUser_returnsNull() throws InternalErrorException {
    assertNull(provider.getByUser(-1));
  }
}
