package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.*;

public class FilesystemVersionProviderTest {

  private static Path tempDir;
  private static FilesystemVersionProvider provider;

  @BeforeAll
  static void init() throws IOException {
    tempDir = Files.createTempDirectory("file-file-provider-test");
    provider = new FilesystemVersionProvider(tempDir.toString() + "/");
  }

  @AfterAll
  static void cleanup() throws IOException {
    Files.walk(tempDir)
        .map(Path::toFile)
        .forEach(
            file -> {
              if (!file.delete()) file.deleteOnExit();
            });
  }

  @Test
  void storeVersionThrowsOnBadId() {
    BadDataException e =
        assertThrows(
            BadDataException.class, () -> provider.storeVersion("some content", new byte[0]));
    assertEquals("Invalid data id", e.getMessage());
  }

  @Test
  void storeVersionCreatesFileAndReturnsId() throws Exception {
    byte[] previous = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
    ByteBuffer buffer = ByteBuffer.wrap(previous);
    long high = buffer.getLong();
    long low = buffer.getLong();
    UUID uuid = new UUID(high, low);
    try (FileWriter writer = new FileWriter(tempDir.resolve(uuid.toString()).toString())) {
      writer.write("content");
    }

    String content = "Hello, world!";
    byte[] id = provider.storeVersion(content, previous);
    assertNotNull(id);
    buffer = ByteBuffer.wrap(id);
    high = buffer.getLong();
    low = buffer.getLong();
    uuid = new UUID(high, low);

    Path storedFile = tempDir.resolve(uuid.toString());
    assertTrue(Files.exists(storedFile));
    assertEquals(content, Files.readString(storedFile));
  }

  @Test
  void storeContentCreatesFileAndReturnsId() throws InternalErrorException, IOException {
    String content = "Hello, world!";
    byte[] id = provider.storeContent(content);
    assertNotNull(id);
    ByteBuffer buffer = ByteBuffer.wrap(id);
    long high = buffer.getLong();
    long low = buffer.getLong();
    UUID uuid = new UUID(high, low);

    Path storedFile = tempDir.resolve(uuid.toString());
    assertTrue(Files.exists(storedFile));
    assertEquals(content, Files.readString(storedFile));
  }

  @Test
  void getVersionReadsStoredContent() throws InternalErrorException, BadDataException {
    String content = "Versioned content test";
    byte[] id = provider.storeContent(content);

    String readContent = provider.getVersion(id);
    assertEquals(content, readContent);
  }

  @Test
  void getVersionThrowsBadDataExceptionForNullId() {
    assertThrows(BadDataException.class, () -> provider.getVersion(null));
  }

  @Test
  void getVersionThrowsBadDataExceptionForNonexistentFile() {
    byte[] fakeId = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
    assertThrows(BadDataException.class, () -> provider.getVersion(fakeId));
  }
}
