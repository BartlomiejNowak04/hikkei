package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.user.UserData;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.*;

public class JDBCUserDataProviderIntegrationTest extends JDBCIntegrationTest {
  private JDBCUserDataProvider provider;

  @BeforeAll
  void initProvider() {
    provider = new JDBCUserDataProvider(connection);
  }

  @Test
  void nonExistentUserDoesNotExist() {
    assertFalse(provider.doesUserExist(2));
  }

  @Test
  void freshlyAddedUserExists() {
    UserData user = new UserData("testname", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);
    assertTrue(userId > 0);
  }
}
