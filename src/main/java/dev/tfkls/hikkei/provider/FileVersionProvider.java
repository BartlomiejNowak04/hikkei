package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.version.VersionProvider;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class FileVersionProvider implements VersionProvider {
  private final String dir;

  public FileVersionProvider(String directory) {
    dir = directory;
  }

  @Override
  public String getContent(UUID id) {
    if (id == null) return null;
    try {
      return Files.readString(Path.of(dir + id));
    } catch (IOException ex) {
      return null;
    }
  }

  @Override
  public UUID storeContent(String content) {
    UUID id = UUID.randomUUID();
    try (FileWriter writer = new FileWriter(dir + id)) {
      writer.write(content);
      return id;
    } catch (IOException ex) {
      return null;
    }
  }
}
