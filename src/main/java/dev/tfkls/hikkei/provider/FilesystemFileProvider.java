package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

public class FilesystemFileProvider implements FileProvider {
  private final Path dir;

  public FilesystemFileProvider(String directory) {
    dir = Paths.get(directory);
    if (!Files.exists(dir) || !Files.isDirectory(dir))
      throw new InvalidPathException(null, "Invalid directory: " + directory);
  }

  @Override
  public byte[] getContent(byte[] id) throws BadDataException, InternalErrorException {
    try {
      String relativePathStr = new String(id);
      Path filePath = dir.resolve(relativePathStr).normalize();
      if (!filePath.startsWith(dir)) throw new BadDataException("Invalid data id");

      return Files.readAllBytes(filePath);
    } catch (NoSuchFileException e) {
      throw new BadDataException("Invalid data id");
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }

  @Override
  public byte[] storeContent(byte[] data) throws InternalErrorException {
    try {
      String filename = UUID.randomUUID().toString();
      Path filePath = dir.resolve(filename);

      Files.write(filePath, data);
      return filename.getBytes();
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }
}
