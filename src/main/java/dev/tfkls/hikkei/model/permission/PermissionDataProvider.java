package dev.tfkls.hikkei.model.permission;

import dev.tfkls.hikkei.model.exceptions.InternalErrorException;

public interface PermissionDataProvider {

  Integer createRole(String role) throws InternalErrorException;

  Integer createCategory(String category) throws InternalErrorException;
}
