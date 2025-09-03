package dev.tfkls.hikkei.model.role;

import java.time.LocalDateTime;

public record RoleData(Integer id, String name, LocalDateTime created) {
  public static final RoleData DELETED = new RoleData(null, null, null);
}
