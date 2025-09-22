package dev.tfkls.hikkei.model.user;

import dev.tfkls.hikkei.model.permission.PermissionLevel;
import java.time.LocalDateTime;

public record UserData(
    Integer id,
    String username,
    byte[] picture,
    LocalDateTime joinedAt,
    PermissionLevel minimalPermission) {
  public UserData(String username, byte[] picture) {
    this(null, username, picture, null, PermissionLevel.NO_ACCESS);
  }

  public UserData(String username) {
    this(username, null);
  }

  public UserData(Integer id, String username, byte[] picture, LocalDateTime joinedAt) {
    this(id, username, picture, joinedAt, PermissionLevel.NO_ACCESS);
  }

  public static final UserData DELETED = new UserData(null, null, null, null, null);
}
