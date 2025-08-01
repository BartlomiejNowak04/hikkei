package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.revision.RevisionData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class JDBCRevisionDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCRevisionDataProvider provider;
  private int pageId;

  @BeforeAll
  void init() throws BadDataException, InternalErrorException {
    provider = new JDBCRevisionDataProvider(connection);

    try (PreparedStatement st =
        connection.prepareStatement("INSERT INTO categories (name) VALUES (?)")) {
      st.setString(1, "test-category");
      st.executeUpdate();
    } catch (SQLException ignored) {
    }

    JDBCPageDataProvider pageProvider = new JDBCPageDataProvider(connection);
    pageId = pageProvider.createPage(new PageData(null, "test-page1", 1, false));
  }

  @Test
  void createRevisionSuccess() throws BadDataException, InternalErrorException {
    RevisionData revision =
        new RevisionData(-1, pageId, UUID.randomUUID(), LocalDateTime.now(), null);

    Integer revId = provider.createRevision(revision);
    assertNotNull(revId);
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

    Integer revId = provider.createRevision(revision);
    assertDoesNotThrow(() -> provider.changeState(revId, pageId, true));
  }

  @Test
  void changeStateFailsForNonexistentRevision() {
    assertThrows(BadDataException.class, () -> provider.changeState(-1, pageId, true));
  }
}
