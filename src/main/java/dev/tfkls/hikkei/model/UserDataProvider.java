package dev.tfkls.hikkei.model;

import java.util.Optional;
import java.util.stream.Stream;

public interface UserDataProvider {
  // Find a user by their properties
  boolean doesUserExist(int userId);
  Optional<Integer> getUserByAuth(AuthData auth);

  // Get the user properties by their handle
  UserData getUserData(int userId) throws BadHandleException;
  Stream<Integer> getUserRoles(int userId) throws BadHandleException;
  Stream<AuthData> getUserAuthMethods(int userId) throws BadHandleException;

  // Modify the user by their handle
  Integer createUser(UserData data) throws BadDataException;
  void updateUser(int userId, UserData data) throws BadHandleException, BadDataException;
  void deleteUser(int userId) throws BadHandleException;

  boolean addUserAuthMethod(int userId, AuthData auth) throws BadHandleException;
  boolean removeUserAuthMethod(int userId, String authType) throws BadHandleException;
  boolean alterUserRole(int userId, int roleId, boolean value) throws BadHandleException;
}
