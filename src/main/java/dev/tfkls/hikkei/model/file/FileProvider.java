package dev.tfkls.hikkei.model.file;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface FileProvider {
  byte[] getContent(byte[] id) throws BadDataException, InternalErrorException;

  byte[] storeContent(byte[] data) throws InternalErrorException;
}
