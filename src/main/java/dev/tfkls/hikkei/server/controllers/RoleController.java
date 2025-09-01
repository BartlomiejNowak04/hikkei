package dev.tfkls.hikkei.server.controllers;

import dev.tfkls.hikkei.model.role.RoleDataProvider;

public class RoleController {
  private final RoleDataProvider roleProvider;

  public RoleController(RoleDataProvider roleProvider) {
    this.roleProvider = roleProvider;
  }
}
