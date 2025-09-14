package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.provider.JDBC.JDBCRevisionDataProvider;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCRevisionDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCRevisionDataProvider provider;
  private int pageId;
  private int noRevisionsId;
  private int userId;

  @BeforeAll
  void init() throws SQLException {
    provider = new JDBCRevisionDataProvider(connection);

    create_category("test-category");
    userId = create_user("testname", null);
    pageId = create_page("test-page1", 1, userId);
    noRevisionsId = create_page("test-page2", 1, userId);
  }

  @Test
  void pageWithNoRevisionsReturnsEmptyList() throws InternalErrorException {
    List<RevisionData> revisions = provider.getByPage(noRevisionsId);
    assertNotNull(revisions);
    assertTrue(revisions.isEmpty());
  }

  @Test
  void createRevisionSuccess() throws BadDataException, InternalErrorException {
    RevisionData revision =
        new RevisionData(-1, pageId, userId, new byte[] {1, 2}, LocalDateTime.now(), null);
    int revId = provider.createRevision(revision);
    assertTrue(revId > 0);
  }

  @Test
  void createRevisionFailsWithInvalidPage() {
    RevisionData revision =
        new RevisionData(-1, -9999, userId, new byte[] {1, 2}, LocalDateTime.now(), null);
    assertThrows(BadDataException.class, () -> provider.createRevision(revision));
  }

  @Test
  void createRevisionFailsWithNullFile() {
    RevisionData revision = new RevisionData(-1, pageId, userId, null, LocalDateTime.now(), null);
    assertThrows(BadDataException.class, () -> provider.createRevision(revision));
  }

  @Test
  void changeStateSuccess() throws BadDataException, InternalErrorException {
    RevisionData revision =
        new RevisionData(-1, pageId, userId, new byte[] {1}, LocalDateTime.now(), null);
    int revId = provider.createRevision(revision);
    assertDoesNotThrow(() -> provider.changeState(revId, true));
    assertDoesNotThrow(() -> provider.changeState(revId, false));
  }

  @Test
  void getByUserReturnsEmptyListForNonexistentUser() throws InternalErrorException {
    List<RevisionData> revisions = provider.getByUser(-1);
    assertNull(revisions);
  }

  @Test
  void getByUserReturnsRevisions() throws BadDataException, InternalErrorException {
    RevisionData revision =
        new RevisionData(-1, pageId, userId, new byte[] {5}, LocalDateTime.now(), null);
    int revId = provider.createRevision(revision);

    List<RevisionData> revisions = provider.getByUser(userId);
    assertNotNull(revisions);
    assertTrue(revisions.stream().anyMatch(r -> r.id() == revId));
  }

  @Test
  void getByIdReturnsRevision() throws BadDataException, InternalErrorException {
    RevisionData revision =
        new RevisionData(-1, pageId, userId, new byte[] {9}, LocalDateTime.now(), null);
    int revId = provider.createRevision(revision);

    RevisionData fetched = provider.getById(revId);
    assertNotNull(fetched);
    assertEquals(revId, fetched.id());
  }

  @Test
  void getByIdReturnsNullForNonexistentRevision() throws InternalErrorException {
    assertNull(provider.getById(-1));
  }

  @Test
  void deleteRevisionMarksDeleted() throws BadDataException, InternalErrorException, SQLException {
    RevisionData revision =
        new RevisionData(-1, pageId, userId, new byte[] {3}, LocalDateTime.now(), null);
    int revId = provider.createRevision(revision);

    provider.deleteRevision(revId);

    try (PreparedStatement st =
        connection.prepareStatement("SELECT deleted FROM revisions WHERE id = ?")) {
      st.setInt(1, revId);
      var rs = st.executeQuery();
      assertTrue(rs.next());
      assertTrue(rs.getBoolean("deleted"));
    }
  }

  @Test
  void draftLifecycle() throws BadDataException, InternalErrorException {
    byte[] draftFile = new byte[] {1, 2, 3};

    provider.saveDraft(userId, pageId, draftFile);

    byte[] fetched = provider.getDraft(userId, pageId);
    assertArrayEquals(draftFile, fetched);

    byte[] updatedFile = new byte[] {4, 5};
    provider.saveDraft(userId, pageId, updatedFile);
    fetched = provider.getDraft(userId, pageId);
    assertArrayEquals(updatedFile, fetched);

    provider.deleteDraft(userId, pageId);
    assertNull(provider.getDraft(userId, pageId));
  }

  @Test
  void saveDraftFailsForNullFile() {
    assertThrows(BadDataException.class, () -> provider.saveDraft(userId, pageId, null));
  }

  @Test
  void saveDraftFailsForInvalidUser() {
    assertThrows(BadDataException.class, () -> provider.saveDraft(-1, pageId, new byte[] {1}));
  }

  @Test
  void saveDraftFailsForInvalidPage() {
    assertThrows(BadDataException.class, () -> provider.saveDraft(userId, -1, new byte[] {1}));
  }
}
