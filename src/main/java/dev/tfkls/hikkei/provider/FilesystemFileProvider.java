package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.StoredFile;
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
  public StoredFile getContent(byte[] id) throws BadDataException, InternalErrorException {
    try {
      String decoded = new String(id);
      int dot = decoded.lastIndexOf('.');
      Path filePath = dir.resolve(decoded).normalize();
      if (!filePath.startsWith(dir)) throw new BadDataException("Invalid data id");

      return new StoredFile(
          Files.readAllBytes(filePath),
          dot == -1 ? null : decoded.substring(dot + 1).replace('$', '/'));
    } catch (NoSuchFileException e) {
      throw new BadDataException("Invalid data id");
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }

  @Override
  public byte[] storeContent(byte[] data, String mime) throws InternalErrorException {
    try {
      String filename = UUID.randomUUID() + (mime == null ? "" : "." + mime.replace('/', '$'));
      Path filePath = dir.resolve(filename);

      Files.write(filePath, data);
      return filename.getBytes();
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }
}
