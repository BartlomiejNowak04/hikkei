package dev.tfkls.hikkei.model.permission;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface PermissionDataProvider {
  PermissionLevel getPermissionLevel(int userId, int categoryId)
      throws InternalErrorException, BadDataException;
}
