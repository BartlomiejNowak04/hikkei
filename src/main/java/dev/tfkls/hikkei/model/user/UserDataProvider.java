package dev.tfkls.hikkei.model.user;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import java.util.List;

public interface UserDataProvider {
  boolean doesUserExist(int userId) throws InternalErrorException;

  UserData getUserByAuth(AuthData auth) throws InternalErrorException;

  UserData getUserData(int userId) throws InternalErrorException;

  List<AuthData> getUserAuthMethods(int userId) throws InternalErrorException;

  int createUser(UserData data) throws BadDataException, InternalErrorException;

  void updateUsername(int userId, String username) throws BadDataException, InternalErrorException;

  void updateUserPicture(int userId, byte[] picture)
      throws BadDataException, InternalErrorException;

  void deleteUser(int userId) throws InternalErrorException;

  void addUserAuthMethod(int userId, AuthData auth) throws BadDataException, InternalErrorException;

  void removeUserAuthMethod(int userId, String authType)
      throws BadDataException, InternalErrorException;
}
