package dev.tfkls.hikkei.model.role;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface RoleDataProvider {
  int createRole(RoleData data) throws InternalErrorException, BadDataException;

  List<RoleData> getRoles() throws InternalErrorException;

  List<RoleData> getByUserId(int userId) throws InternalErrorException;

  RoleData getById(int id) throws InternalErrorException;

  void changeName(int id, String name) throws InternalErrorException, BadDataException;

  void deleteRole(int id) throws InternalErrorException;
}
