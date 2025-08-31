package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.user.UserDataProvider;

public class UserController {
  private final UserDataProvider userProvider;

  public UserController(UserDataProvider provider) {
    this.userProvider = provider;
  }
}
