package dev.tfkls.hikkei.model.file;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface FileProvider {
  StoredFile getContent(byte[] id) throws InternalErrorException;

  byte[] storeContent(byte[] data, String mime) throws InternalErrorException;

  void deleteContent(byte[] id) throws InternalErrorException;
}
