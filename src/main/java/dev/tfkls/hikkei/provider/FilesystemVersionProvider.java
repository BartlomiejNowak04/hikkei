package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.VersionProvider;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.UUID;

public class FilesystemVersionProvider implements VersionProvider {
  private final Path dir;

  public FilesystemVersionProvider(String directory) {
    dir = Paths.get(directory);
    if (!Files.exists(dir) || !Files.isDirectory(dir))
      throw new InvalidPathException(null, "Invalid directory: " + directory);
  }

  @Override
  public String getVersion(byte[] id) throws BadDataException, InternalErrorException {
    if (id == null || id.length != 16) throw new BadDataException("Invalid data id");
    ByteBuffer buffer = ByteBuffer.wrap(id);
    long high = buffer.getLong();
    long low = buffer.getLong();
    UUID uuid = new UUID(high, low);

    try {
      return Files.readString(dir.resolve(uuid.toString()));
    } catch (NoSuchFileException e) {
      throw new BadDataException("Invalid data id");
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }

  @Override
  public byte[] storeContent(String content) throws InternalErrorException {
    UUID id = UUID.randomUUID();
    try (FileWriter writer = new FileWriter(dir.resolve(id.toString()).toFile())) {
      writer.write(content);
      ByteBuffer buffer = ByteBuffer.allocate(16);
      buffer.putLong(id.getMostSignificantBits());
      buffer.putLong(id.getLeastSignificantBits());
      return buffer.array();
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }

  @Override
  public byte[] storeVersion(String content, byte[] previous)
      throws BadDataException, InternalErrorException {
    getVersion(previous);
    return storeContent(content);
  }
}
