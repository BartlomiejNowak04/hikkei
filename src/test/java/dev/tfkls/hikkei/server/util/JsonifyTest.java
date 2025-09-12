package dev.tfkls.hikkei.server.util;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.comment.CommentData;
import dev.tfkls.hikkei.model.page.PageData;
import dev.tfkls.hikkei.model.revision.RevisionData;
import dev.tfkls.hikkei.model.user.UserData;
import io.vertx.core.json.JsonObject;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class JsonifyTest {

  @Test
  void pageToJsonShouldMapCorrectly() {
    LocalDateTime now = LocalDateTime.now();
    PageData page = new PageData(1, 2, "Test Page", 42, false, now);

    JsonObject json = Jsonify.pageToJson(page);

    assertEquals(1, json.getInteger("id"));
    assertEquals(2, json.getInteger("user_id"));
    assertEquals("Test Page", json.getString("title"));
    assertEquals(42, json.getInteger("category"));
    assertFalse(json.getBoolean("archived"));
    assertEquals(now, LocalDateTime.parse(json.getString("created")));
  }

  @Test
  void revisionToJsonShouldMapCorrectly() {
    LocalDateTime now = LocalDateTime.now();
    RevisionData revision = new RevisionData(5, 2, 1, new byte[] {0}, now, true);

    JsonObject json = Jsonify.revisionToJson(revision);

    assertEquals(5, json.getInteger("id"));
    assertEquals(2, json.getInteger("page_id"));
    assertEquals(1, json.getInteger("user_id"));
    assertArrayEquals(new byte[] {0}, json.getBinary("filename"));
    assertEquals(now, LocalDateTime.parse(json.getString("date")));
    assertTrue(json.getBoolean("approved"));
  }

  @Test
  void commentToJsonShouldMapCorrectly() {
    LocalDateTime now = LocalDateTime.now();
    CommentData comment = new CommentData(10, 1, 1, "Hello world", now);

    JsonObject json = Jsonify.commentToJson(comment);

    assertEquals(10, json.getInteger("id"));
    assertEquals(1, json.getInteger("page_id"));
    assertEquals(1, json.getInteger("user_id"));
    assertEquals("Hello world", json.getString("content"));
    assertEquals(now, LocalDateTime.parse(json.getString("date")));
  }

  @Test
  void userToJsonShouldMapCorrectly() {
    LocalDateTime now = LocalDateTime.now();
    UserData user = new UserData(10, "name", new byte[0], now);

    JsonObject json = Jsonify.userToJson(user);

    assertEquals(10, json.getInteger("id"));
    assertEquals("name", json.getString("username"));
    assertArrayEquals(new byte[0], json.getBinary("picture"));
    assertEquals(now, LocalDateTime.parse(json.getString("joined")));
  }

  @Test
  void pageToJsonHandlesNullFields() {
    PageData page = new PageData(null, null, null, null, null, null);

    JsonObject json = Jsonify.pageToJson(page);

    assertTrue(json.containsKey("id"));
    assertNull(json.getValue("id"));
    assertTrue(json.containsKey("user_id"));
    assertNull(json.getValue("user_id"));
    assertTrue(json.containsKey("title"));
    assertNull(json.getValue("title"));
    assertTrue(json.containsKey("category"));
    assertNull(json.getValue("category"));
    assertTrue(json.containsKey("archived"));
    assertNull(json.getValue("archived"));
    assertTrue(json.containsKey("created"));
    assertNull(json.getValue("created"));
  }

  @Test
  void revisionToJsonHandlesNullFields() {
    RevisionData revision = new RevisionData(null, null, null, null, null, null);

    JsonObject json = Jsonify.revisionToJson(revision);

    assertTrue(json.containsKey("id"));
    assertNull(json.getValue("id"));
    assertTrue(json.containsKey("page_id"));
    assertNull(json.getValue("page_id"));
    assertTrue(json.containsKey("user_id"));
    assertNull(json.getValue("user_id"));
    assertTrue(json.containsKey("filename"));
    assertNull(json.getValue("filename"));
    assertTrue(json.containsKey("date"));
    assertNull(json.getValue("date"));
    assertTrue(json.containsKey("approved"));
    assertNull(json.getValue("approved"));
  }

  @Test
  void commentToJsonHandlesNullFields() {
    CommentData comment = new CommentData(null, null, null, null, null);

    JsonObject json = Jsonify.commentToJson(comment);

    assertTrue(json.containsKey("id"));
    assertNull(json.getValue("id"));
    assertTrue(json.containsKey("page_id"));
    assertNull(json.getValue("page_id"));
    assertTrue(json.containsKey("user_id"));
    assertNull(json.getValue("user_id"));
    assertTrue(json.containsKey("content"));
    assertNull(json.getValue("content"));
    assertTrue(json.containsKey("date"));
    assertNull(json.getValue("date"));
  }

  @Test
  void userToJsonHandlesNullFields() {
    UserData user = new UserData(null, null, null, null);

    JsonObject json = Jsonify.userToJson(user);

    assertTrue(json.containsKey("id"));
    assertNull(json.getValue("id"));
    assertTrue(json.containsKey("username"));
    assertNull(json.getValue("username"));
    assertTrue(json.containsKey("picture"));
    assertNull(json.getValue("picture"));
    assertTrue(json.containsKey("joined"));
    assertNull(json.getValue("joined"));
  }
}
