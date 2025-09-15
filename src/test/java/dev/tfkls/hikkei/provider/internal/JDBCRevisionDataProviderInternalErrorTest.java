package dev.tfkls.hikkei.provider.internal;

import static dev.tfkls.hikkei.provider.JDBC.Config.internal_error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.provider.JDBC.JDBCRevisionDataProvider;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class JDBCRevisionDataProviderInternalErrorTest {
  private JDBCRevisionDataProvider provider;

  @BeforeAll
  void setUp() {
    Connection connection =
        mock(
            Connection.class,
            invocation -> {
              throw new SQLException("test");
            });
    provider = new JDBCRevisionDataProvider(connection);
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

  @Test
  void changeStateThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.changeState(1, true));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void createRevisionThrowsInternalError() {
    RevisionData rev = new RevisionData(1, 1, new byte[] {});
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.createRevision(rev));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void deleteRevisionThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.deleteRevision(1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void saveDraftThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.saveDraft(1, 1, new byte[] {}));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void getDraftThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.getDraft(1, 1));
    assertEquals(internal_error, ex.getMessage());
  }

  @Test
  void deleteDraftThrowsInternalError() {
    InternalErrorException ex =
        assertThrows(InternalErrorException.class, () -> provider.deleteDraft(1, 1));
    assertEquals(internal_error, ex.getMessage());
  }
}
