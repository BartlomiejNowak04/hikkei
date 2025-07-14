package dev.tfkls.hikkei.model.version;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.UUID;

public interface VersionProvider {
  String getContent(UUID id) throws BadDataException, InternalErrorException;

  UUID storeContent(String content) throws InternalErrorException;
}
