package dev.tfkls.hikkei.model.user;

import java.util.List;
import java.util.Optional;

public interface UserDataProvider {
  // Find a user by their properties
  boolean doesUserExist(int userId);

  Optional<Integer> getUserByAuth(AuthData auth);

  // Get the user properties by their handle
  UserData getUserData(int userId) throws BadHandleException;

  List<Integer> getUserRoles(int userId) throws BadHandleException;

  List<AuthData> getUserAuthMethods(int userId) throws BadHandleException;

  // Modify the user by their handle
  Integer createUser(UserData data) throws BadDataException;

  void updateUsername(int userId, String username) throws BadHandleException, BadDataException;

  void updateUserPicture(int userId, byte[] picture) throws BadHandleException;

  void deleteUser(int userId) throws BadHandleException;

  boolean addUserAuthMethod(int userId, AuthData auth) throws BadHandleException;

  boolean removeUserAuthMethod(int userId, String authType) throws BadHandleException;

  boolean alterUserRole(int userId, int roleId, boolean value) throws BadHandleException;
}
