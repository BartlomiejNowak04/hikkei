package dev.tfkls.hikkei.model;

import java.util.Optional;

public class UserManager {
  private final UserDataProvider provider;

  public UserManager(UserDataProvider provider) {
    this.provider = provider;
  }

  Optional<UserHandle> getUserById(int userId) {
    return provider.doesUserExist(userId) ? Optional.of(new UserHandle(userId)) : Optional.empty();
  }
  Optional<UserHandle> getUserByAuth(AuthData auth) {
    return provider.getUserByAuth(auth).map(UserHandle::new);
  }
}

