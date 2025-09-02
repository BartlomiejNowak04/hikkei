package dev.tfkls.hikkei.provider;

import static java.nio.charset.StandardCharsets.UTF_8;

import dev.tfkls.hikkei.test.IntegrationTest;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class JDBCIntegrationTest extends IntegrationTest {
  protected Connection connection;

  @BeforeAll
  void initDatabase() throws SQLException, IOException {
    String host = require("POSTGRES_HOST");
    String name = require("POSTGRES_NAME");
    String user = require("POSTGRES_USER");
    String password = require("POSTGRES_PASS");
    connection =
        DriverManager.getConnection("jdbc:postgresql://" + host + "/" + name, user, password);

    String base = "/dev/tfkls/hikkei/database/";
    for (String resource :
        List.of(
            base + "drop.sql",
            base + "schema.sql",
            base + "views.sql",
            base + "rules.sql",
            base + "triggers.sql")) {
      String schema;
      try (InputStream schemaStream = getClass().getResourceAsStream(resource)) {
        Assumptions.assumeTrue(schemaStream != null);
        schema = new String(schemaStream.readAllBytes(), UTF_8);
      }

      try (Statement stmt = connection.createStatement()) {
        stmt.execute(schema);
      }
    }
  }

  @AfterAll
  void tearDown() throws SQLException {
    if (connection != null) connection.close();
  }
}
