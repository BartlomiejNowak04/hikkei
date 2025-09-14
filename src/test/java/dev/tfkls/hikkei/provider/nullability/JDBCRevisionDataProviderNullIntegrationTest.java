package dev.tfkls.hikkei.provider.nullability;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.revision.RevisionDataProvider;
import dev.tfkls.hikkei.provider.JDBC.Config;
import dev.tfkls.hikkei.provider.JDBC.JDBCRevisionDataProvider;
import dev.tfkls.hikkei.provider.JDBCIntegrationTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCRevisionDataProviderNullIntegrationTest extends JDBCIntegrationTest {
  private RevisionDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCRevisionDataProvider(connection);
  }

  @Test
  void createRevision_nullData_throwsBadDataException() {
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createRevision(null));
    assertEquals(Config.invalid_data, ex.getMessage());
  }

  @Test
  void createRevision_nullPageId_throwsBadDataException() {
    RevisionData data = new RevisionData(0, null, 1, new byte[] {1}, LocalDateTime.now(), null);
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createRevision(data));
    assertEquals(Config.invalid_page, ex.getMessage());
  }

  @Test
  void createRevision_nullFile_throwsBadDataException() {
    RevisionData data = new RevisionData(0, 1, 1, null, LocalDateTime.now(), null);
    BadDataException ex = assertThrows(BadDataException.class, () -> provider.createRevision(data));
    assertEquals(Config.invalid_datafile, ex.getMessage());
  }

  @Test
  void getByPage_invalidPage_returnsNull() throws InternalErrorException {
    assertNull(provider.getByPage(-1));
  }

  @Test
  void getByUser_invalidUser_returnsNull() throws InternalErrorException {
    assertNull(provider.getByUser(-1));
  }

  @Test
  void getById_notExisting_returnsNull() throws InternalErrorException {
    assertNull(provider.getById(-1));
  }

  @Test
  void getDraft_notExisting_returnsNull() throws InternalErrorException {
    assertNull(provider.getDraft(-1, -1));
  }

  @Test
  void saveDraft_nullFile_throwsBadDataException() {
    BadDataException ex =
        assertThrows(BadDataException.class, () -> provider.saveDraft(1, 1, null));
    assertEquals(Config.invalid_datafile, ex.getMessage());
  }
}
