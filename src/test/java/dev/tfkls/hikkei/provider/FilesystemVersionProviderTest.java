package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
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
            BadDataException.class,
            () -> provider.storeVersion("some content", "nonexistent".getBytes()));
    assertEquals("Invalid version id", e.getMessage());
  }

  @Test
  void storeVersionCreatesFileAndReturnsId() throws Exception {
    byte[] previous = "firstfile".getBytes();
    try (FileWriter writer = new FileWriter(tempDir.resolve("firstfile").toFile())) {
      writer.write("content");
    }

    String content = "Hello, world!";
    byte[] id = provider.storeVersion(content, previous);

    Path storedFile = tempDir.resolve(new String(id));
    assertTrue(Files.exists(storedFile));
    assertEquals(content, Files.readString(storedFile));
  }

  @Test
  void storeContentCreatesFileAndReturnsId() throws InternalErrorException, IOException {
    String content = "Hello, world!";
    byte[] id = provider.storeContent(content);
    assertNotNull(id);

    Path storedFile = tempDir.resolve(new String(id));
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
  void getVersionReturnsNullForNullId() throws Exception {
    assertNull(provider.getVersion(null));
  }

  @Test
  void getVersionReturnsNullForNonexistentFile() throws Exception {
    byte[] fakeId = {'1', '2', '3', '4', '5', '6', '7', '8', '9'};
    assertNull(provider.getVersion(fakeId));
  }
}
