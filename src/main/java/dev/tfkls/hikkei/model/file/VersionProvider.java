package dev.tfkls.hikkei.model.file;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface VersionProvider {
  StoredVersion getVersion(byte[] id) throws InternalErrorException;

  byte[] storeContent(String content, String mime) throws InternalErrorException;

  byte[] storeVersion(String content, byte[] previous)
      throws BadDataException, InternalErrorException;

  void deleteVersion(byte[] id) throws InternalErrorException;
}
