package dev.tfkls.hikkei.provider;

import static dev.tfkls.hikkei.provider.JDBC.Config.*;
import static java.nio.charset.StandardCharsets.UTF_8;

import dev.tfkls.hikkei.test.IntegrationTest;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
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

  protected int create_user(String username, byte[] picture) throws SQLException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO users_with_data (username, picture) VALUES (?, ?)",
            Statement.RETURN_GENERATED_KEYS)) {
      st.setString(1, username);
      st.setBytes(2, picture);
      st.executeUpdate();
      st.getGeneratedKeys().next();
      return st.getGeneratedKeys().getInt(1);
    }
  }

  protected int create_category(String name) throws SQLException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO categories_with_data (name) VALUES (?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setString(1, name);
      st.executeUpdate();
      st.getGeneratedKeys().next();
      return st.getGeneratedKeys().getInt(1);
    }
  }

  protected int create_page(String title, int category, int user) throws SQLException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO pages_with_data (user_id, title, category) VALUES (?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, user);
      st.setString(2, title);
      st.setInt(3, category);
      st.executeUpdate();
      st.getGeneratedKeys().next();
      return st.getGeneratedKeys().getInt(1);
    }
  }

  protected int create_revision(int page, int user, byte[] file) throws SQLException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO revisions_with_data (page_id, user_id, file) VALUES (?, ?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, page);
      st.setInt(2, user);
      st.setObject(3, file);
      st.executeUpdate();
      st.getGeneratedKeys().next();
      return st.getGeneratedKeys().getInt(1);
    }
  }

  protected int create_comment(int page, String content) throws SQLException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "INSERT INTO comments (page_id, content) VALUES (?, ?)",
            PreparedStatement.RETURN_GENERATED_KEYS)) {
      st.setInt(1, page);
      st.setString(2, content);
      st.executeUpdate();
      st.getGeneratedKeys().next();
      return st.getGeneratedKeys().getInt(1);
    }
  }

  protected void delete(String type, int id) throws SQLException {
    try (PreparedStatement st =
        connection.prepareStatement(
            "UPDATE " + type + "s SET deleted=true, date_deleted=now() WHERE id=?")) {
      st.setInt(1, id);
      st.executeUpdate();
    }
  }
}
