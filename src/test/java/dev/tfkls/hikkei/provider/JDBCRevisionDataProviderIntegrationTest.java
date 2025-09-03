package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.revision.RevisionData;
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

  @BeforeAll
  void init() throws SQLException {
    provider = new JDBCRevisionDataProvider(connection);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO categories_with_data (name) VALUES (?)")) {
      st.setString(1, "test-category");
      st.executeUpdate();
    }

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO users_with_data (username) VALUES (?)")) {
      st.setString(1, "test-user");
      st.executeUpdate();
    }

    PreparedStatement st1 =
        connection.prepareStatement(
            "INSERT INTO pages_with_data (title, category, user_id) VALUES (?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS);
    st1.setString(1, "test-page1");
    st1.setInt(2, 1);
    st1.setInt(3, 1);
    st1.executeUpdate();

    st1.getGeneratedKeys().next();
    pageId = st1.getGeneratedKeys().getInt(1);

    st1.setString(1, "no-revisions");
    st1.setInt(2, 1);
    st1.executeUpdate();

    st1.getGeneratedKeys().next();
    noRevisionsId = st1.getGeneratedKeys().getInt(1);
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
        new RevisionData(-1, pageId, 1, new byte[] {}, LocalDateTime.now(), null);

    int revId = provider.createRevision(revision);
    assertTrue(revId > 0);
  }

  @Test
  void createRevisionFailsWithInvalidPage() {
    RevisionData revision =
        new RevisionData(-1, -9999, 1, new byte[] {}, LocalDateTime.now(), null);

    assertThrows(BadDataException.class, () -> provider.createRevision(revision));
  }

  @Test
  void changeStateSuccess() throws BadDataException, InternalErrorException {
    RevisionData revision =
        new RevisionData(-1, pageId, 1, new byte[] {}, LocalDateTime.now(), null);

    int revId = provider.createRevision(revision);
    assertDoesNotThrow(() -> provider.changeState(revId, true));
  }

  @Test
  void changeStateFailsForNonexistentRevision() {
    assertThrows(BadDataException.class, () -> provider.changeState(-1, true));
  }
}
