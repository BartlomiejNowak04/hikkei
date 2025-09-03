package dev.tfkls.hikkei.model.version;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface VersionProvider {
  String getContent(byte[] id) throws BadDataException, InternalErrorException;

  byte[] storeContent(String content) throws InternalErrorException;
}
