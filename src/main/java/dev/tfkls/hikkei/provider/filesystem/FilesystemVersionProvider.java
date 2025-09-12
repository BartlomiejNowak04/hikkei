package dev.tfkls.hikkei.provider.filesystem;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import dev.tfkls.hikkei.model.file.FileProvider;
import dev.tfkls.hikkei.model.file.StoredFile;
import dev.tfkls.hikkei.model.file.StoredVersion;
import dev.tfkls.hikkei.model.file.VersionProvider;

public class FilesystemVersionProvider implements VersionProvider {
  private final FileProvider fileProvider;

  public FilesystemVersionProvider(String directory) {
    fileProvider = new FilesystemFileProvider(directory);
  }

  @Override
  public StoredVersion getVersion(byte[] id) throws InternalErrorException {
    StoredFile file = fileProvider.getContent(id);
    if (file == null) return null;
    return new StoredVersion(new String(file.data()), file.mime());
  }

  @Override
  public byte[] storeContent(String content, String mime) throws InternalErrorException {
    return fileProvider.storeContent(content.getBytes(), mime);
  }

  @Override
  public byte[] storeVersion(String content, byte[] previous)
      throws BadDataException, InternalErrorException {
    StoredVersion prev = getVersion(previous);
    if (previous != null && prev == null) throw new BadDataException("Invalid version id");
    return storeContent(content, prev.mime());
  }

  @Override
  public void deleteVersion(byte[] id) throws InternalErrorException {
    fileProvider.deleteContent(id);
  }
}
