package dev.tfkls.hikkei.model.category;

import dev.tfkls.hikkei.model.permission.PermissionLevel;
import java.time.LocalDateTime;

public record CategoryData(
    Integer id, String name, LocalDateTime created, PermissionLevel defaultPermission) {
  public CategoryData(String name) {
    this(null, name, null, PermissionLevel.UNTRUSTED);
  }

  public CategoryData(Integer id, String name, LocalDateTime created) {
    this(id, name, created, PermissionLevel.UNTRUSTED);
  }

  public static final CategoryData DELETED = new CategoryData(null, null, null, null);
}
