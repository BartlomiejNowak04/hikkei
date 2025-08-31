package dev.tfkls.hikkei.model.permission;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface PermissionDataProvider {

  int createRole(String role) throws InternalErrorException;

  int createCategory(String category) throws InternalErrorException;
}
