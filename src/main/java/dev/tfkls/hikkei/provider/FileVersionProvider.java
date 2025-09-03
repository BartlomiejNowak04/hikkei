package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.version.VersionProvider;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
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
  public String getContent(byte[] id) throws BadDataException, InternalErrorException {
    if (id == null || id.length != 16) throw new BadDataException("Invalid version id");
    ByteBuffer buffer = ByteBuffer.wrap(id);
    long high = buffer.getLong();
    long low = buffer.getLong();
    UUID uuid = new UUID(high, low);

    try {
      return Files.readString(Path.of(dir, uuid.toString()));
    } catch (NoSuchFileException e) {
      throw new BadDataException("Invalid version id");
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }

  @Override
  public byte[] storeContent(String content) throws InternalErrorException {
    UUID id = UUID.randomUUID();
    try (FileWriter writer = new FileWriter(dir + id)) {
      writer.write(content);
      ByteBuffer buffer = ByteBuffer.allocate(16);
      buffer.putLong(id.getMostSignificantBits());
      buffer.putLong(id.getLeastSignificantBits());
      return buffer.array();
    } catch (IOException e) {
      throw new InternalErrorException("Filesystem internal error");
    }
  }
}
