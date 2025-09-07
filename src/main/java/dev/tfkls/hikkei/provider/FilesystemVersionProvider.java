package dev.tfkls.hikkei.provider;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.model.file.VersionProvider;

public class FilesystemVersionProvider implements VersionProvider {
  private final FileProvider fileProvider;

  public FilesystemVersionProvider(String directory) {
    fileProvider = new FilesystemFileProvider(directory);
  }

  @Override
  public String getVersion(byte[] id) throws InternalErrorException {
    StoredFile file = fileProvider.getContent(id);
    if (file == null) return null;
    return new String(file.data());
  }

  @Override
  public byte[] storeContent(String content) throws InternalErrorException {
    return fileProvider.storeContent(content.getBytes(), null);
  }

  @Override
  public byte[] storeVersion(String content, byte[] previous)
      throws BadDataException, InternalErrorException {
    if (previous != null && getVersion(previous) == null)
      throw new BadDataException("Invalid version id");
    return storeContent(content);
  }

  @Override
  public void deleteVersion(byte[] id) throws InternalErrorException {
    fileProvider.deleteContent(id);
  }
}
