package dev.tfkls.hikkei.model.user;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface UserDataProvider {
  // Find a user by their properties
  boolean doesUserExist(int userId) throws InternalErrorException;

  Integer getUserByAuth(AuthData auth) throws InternalErrorException;

  // Get the user properties by their handle
  UserData getUserData(int userId) throws InternalErrorException;

  List<Integer> getUserRoles(int userId) throws InternalErrorException;

  List<AuthData> getUserAuthMethods(int userId) throws InternalErrorException;

  // Modify the user by their handle
  Integer createUser(UserData data) throws BadDataException, InternalErrorException;

  void updateUsername(int userId, String username) throws BadDataException, InternalErrorException;

  void updateUserPicture(int userId, byte[] picture)
      throws BadDataException, InternalErrorException;

  void deleteUser(int userId) throws BadDataException, InternalErrorException;

  boolean addUserAuthMethod(int userId, AuthData auth)
      throws BadDataException, InternalErrorException;

  boolean removeUserAuthMethod(int userId, String authType)
      throws BadDataException, InternalErrorException;

  boolean alterUserRole(int userId, int roleId, boolean value)
      throws BadDataException, InternalErrorException;
}
