package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.*;

public class FileVersionProviderTest {

  private static Path tempDir;
  private static FileVersionProvider provider;

  @BeforeAll
  static void init() throws IOException {
    tempDir = Files.createTempDirectory("file-version-provider-test");
    provider = new FileVersionProvider(tempDir.toString() + "/");
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
  void storeContentCreatesFileAndReturnsUUID() throws InternalErrorException, IOException {
    String content = "Hello, world!";
    UUID id = provider.storeContent(content);

    assertNotNull(id);
    Path storedFile = tempDir.resolve(id.toString());
    assertTrue(Files.exists(storedFile));
    assertEquals(content, Files.readString(storedFile));
  }

  @Test
  void getContentReadsStoredContent() throws InternalErrorException, BadDataException {
    String content = "Versioned content test";
    UUID id = provider.storeContent(content);

    String readContent = provider.getContent(id);
    assertEquals(content, readContent);
  }

  @Test
  void getContentThrowsBadDataExceptionForNullId() {
    assertThrows(BadDataException.class, () -> provider.getContent(null));
  }

  @Test
  void getContentThrowsBadDataExceptionForNonexistentFile() {
    UUID fakeId = UUID.randomUUID();
    assertThrows(BadDataException.class, () -> provider.getContent(fakeId));
  }
}
