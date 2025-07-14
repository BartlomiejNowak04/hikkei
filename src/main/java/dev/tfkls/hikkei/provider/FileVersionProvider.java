package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.version.VersionProvider;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;

public class FileVersionProvider implements VersionProvider {
  private final String dir;

  public FileVersionProvider(String directory) {
    dir = directory;
  }

  @Override
  public String getContent(UUID id) throws BadDataException, InternalErrorException {
    if (id == null) throw new BadDataException("Invalid version id");
    try {
      return Files.readString(Path.of(dir, id.toString()));
    } catch (NoSuchFileException e) {
      throw new BadDataException("Invalid version id");
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }

  @Override
  public UUID storeContent(String content) throws InternalErrorException {
    UUID id = UUID.randomUUID();
    try (FileWriter writer = new FileWriter(dir + id)) {
      writer.write(content);
      return id;
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }
}
