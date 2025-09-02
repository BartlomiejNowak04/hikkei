package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCRevisionDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCRevisionDataProvider provider;
  private int pageId;
  private int noRevisionsId;

  @BeforeAll
  void init() throws SQLException {
    provider = new JDBCRevisionDataProvider(connection);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
      st.setString(1, "test-category");
      st.executeUpdate();
    } catch (SQLException ignored) {
    }

    PreparedStatement st1 =
        connection.prepareStatement(
            "INSERT INTO pages (title, category) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
    st1.setString(1, "test-page1");
    st1.setInt(2, 1);
    st1.executeUpdate();

    st1.getGeneratedKeys().next();
    pageId = st1.getGeneratedKeys().getInt(1);

    PreparedStatement st2 =
        connection.prepareStatement(
            "INSERT INTO pages (title, category) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
    st2.setString(1, "no-revisions");
    st2.setInt(2, 1);
    st2.executeUpdate();

    st2.getGeneratedKeys().next();
    noRevisionsId = st2.getGeneratedKeys().getInt(1);
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
        new RevisionData(-1, pageId, UUID.randomUUID(), LocalDateTime.now(), null);

    int revId = provider.createRevision(revision);
    assertTrue(revId > 0);
  }

  @Test
  void createRevisionFailsWithInvalidPage() {
    RevisionData revision =
        new RevisionData(-1, -9999, UUID.randomUUID(), LocalDateTime.now(), null);

    assertThrows(BadDataException.class, () -> provider.createRevision(revision));
  }

  @Test
  void changeStateSuccess() throws BadDataException, InternalErrorException {
    RevisionData revision =
        new RevisionData(-1, pageId, UUID.randomUUID(), LocalDateTime.now(), null);

    int revId = provider.createRevision(revision);
    assertDoesNotThrow(() -> provider.changeState(revId, pageId, true));
  }

  @Test
  void changeStateFailsForNonexistentRevision() {
    assertThrows(BadDataException.class, () -> provider.changeState(-1, pageId, true));
  }
}
