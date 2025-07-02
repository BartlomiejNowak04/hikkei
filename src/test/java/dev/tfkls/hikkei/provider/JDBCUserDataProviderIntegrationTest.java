package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.user.UserData;
import io.github.cdimascio.dotenv.Dotenv;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JDBCUserDataProviderIntegrationTest {

  private Connection connection;
  private JDBCUserDataProvider provider;

  @BeforeAll
  void initDatabase() throws SQLException {
    Dotenv dotenv = Dotenv.load();
    String host = dotenv.get("POSTGRES_HOST");
    String db = dotenv.get("POSTGRES_TESTDB");
    String user = dotenv.get("POSTGRES_USER");
    String password = dotenv.get("POSTGRES_PASS");
    connection =
        DriverManager.getConnection("jdbc:postgresql://" + host + "/" + db, user, password);
    provider = new JDBCUserDataProvider(connection);

    try (Statement stmt = connection.createStatement()) {
      stmt.execute(
          """
                DROP TABLE IF EXISTS users, auth, roles, user_role CASCADE;

                CREATE TABLE users (
                    id SERIAL PRIMARY KEY,
                    username VARCHAR(125) UNIQUE NOT NULL,
                    picture BYTEA,
                    joined TIMESTAMP NOT NULL DEFAULT now(),
                    deleted BOOLEAN NOT NULL DEFAULT false
                );

                CREATE TABLE auth (
                    user_id INTEGER REFERENCES users(id),
                    type VARCHAR(125),
                    PRIMARY KEY (user_id, type),
                    token TEXT NOT NULL,
                    UNIQUE(type, token)
                );

                CREATE TABLE roles (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(125) NOT NULL
                );

                CREATE TABLE user_role (
                    user_id INTEGER REFERENCES users(id),
                    role_id INTEGER REFERENCES roles(id),
                    PRIMARY KEY (user_id, role_id)
                );
            """);
    }
  }

  @Test
  void unexistingUserDoesntExist() {
    assertFalse(provider.doesUserExist(2));
  }

  @Test
  void creatingUserWorks() {
    UserData user = new UserData("testname", Optional.empty(), LocalDateTime.now(), false);
    Integer userId = provider.createUser(user);
    assertTrue(userId > 0);
  }

  @AfterAll
  void tearDown() throws SQLException {
    connection.close();
  }
}
