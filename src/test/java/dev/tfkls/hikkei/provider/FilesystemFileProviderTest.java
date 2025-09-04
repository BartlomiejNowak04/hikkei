package dev.tfkls.hikkei.provider;

import static org.junit.jupiter.api.Assertions.*;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
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

    byte[] id = provider.storeContent(data);
    assertNotNull(id);

    Path file = tempDir.resolve(new String(id));
    assertTrue(Files.exists(file));
    assertArrayEquals(data, Files.readAllBytes(file));
  }

  @Test
  void testNoGoingUp() {
    BadDataException e =
        assertThrows(
            BadDataException.class,
            () -> {
              provider.getContent("../etc/passwd".getBytes());
            });
    assertEquals("Invalid data id", e.getMessage());
  }

  @Test
  void testNonExistentFileThrowsBadDataException() {
    BadDataException e =
        assertThrows(
            BadDataException.class,
            () -> {
              provider.getContent("nonexistent".getBytes());
            });
    assertEquals("Invalid data id", e.getMessage());
  }

  @Test
  void testStoreGeneratesUniqueIds() throws Exception {
    byte[] data1 = "File1".getBytes();
    byte[] data2 = "File2".getBytes();

    byte[] id1 = provider.storeContent(data1);
    byte[] id2 = provider.storeContent(data2);

    assertNotEquals(new String(id1), new String(id2));
  }
}
