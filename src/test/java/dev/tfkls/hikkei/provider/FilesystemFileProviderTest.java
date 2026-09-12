package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.provider.filesystem.FilesystemFileProvider;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.*;

class FilesystemFileProviderTest {
  private Path tempDir;
  private FilesystemFileProvider provider;

  @BeforeEach
  void setUp() throws IOException {
    tempDir = Files.createTempDirectory("fs-provider-test");
    provider = new FilesystemFileProvider(tempDir.toString());
  }

  @AfterEach
  void tearDown() throws IOException {
    Files.walk(tempDir)
        .map(Path::toFile)
        .forEach(
            file -> {
              if (!file.delete()) file.deleteOnExit();
            });
  }

  @Test
  void testStoreContent() throws Exception {
    byte[] data = "Hello world".getBytes();

    byte[] id = provider.storeContent(data, null);
    assertNotNull(id);

    Path file = tempDir.resolve(new String(id));
    assertTrue(Files.exists(file));
    assertArrayEquals(data, Files.readAllBytes(file));
  }

  @Test
  void mimeGetsSaved() throws Exception {
    byte[] data = "Hello world".getBytes();
    byte[] id = provider.storeContent(data, "bin");
    StoredFile file = provider.getContent(id);
    assertEquals("bin", file.mime());
    assertArrayEquals(data, file.data());
  }

  @Test
  void testNoGoingUp() throws Exception {
    assertNull(provider.getContent("../etc/passwd".getBytes()));
  }

  @Test
  void testNonExistentFileReturnsNull() throws Exception {
    assertNull(provider.getContent("nonexistent".getBytes()));
  }

  @Test
  void deleteContentRemovesStoredFile() throws Exception {
    byte[] id = provider.storeContent("bye".getBytes(), "text/plain");
    Path file = tempDir.resolve(new String(id).split("\\$")[0]);
    assertTrue(Files.exists(file));

    provider.deleteContent(id);

    assertFalse(Files.exists(file));
    assertNull(provider.getContent(id));
  }

  @Test
  void deleteContentIgnoresNullId() {
    assertDoesNotThrow(() -> provider.deleteContent(null));
  }

  @Test
  void deleteContentIgnoresMissingFile() {
    assertDoesNotThrow(() -> provider.deleteContent("nonexistent".getBytes()));
  }

  @Test
  void deleteContentNeverLeavesDirectory() throws Exception {
    Path outside = Files.createTempFile("fs-provider-outside", ".txt");
    try {
      provider.deleteContent(("../" + outside.getFileName()).getBytes());
      assertTrue(Files.exists(outside));
    } finally {
      Files.deleteIfExists(outside);
    }
  }

  @Test
  void testStoreGeneratesUniqueIds() throws Exception {
    byte[] data1 = "File1".getBytes();
    byte[] data2 = "File2".getBytes();

    byte[] id1 = provider.storeContent(data1, null);
    byte[] id2 = provider.storeContent(data2, null);

    assertNotEquals(new String(id1), new String(id2));
  }
}
