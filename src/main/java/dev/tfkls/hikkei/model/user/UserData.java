package dev.tfkls.hikkei.model.user;

import java.time.LocalDateTime;

public record UserData(Integer id, String username, byte[] picture, LocalDateTime joinedAt) {
  public UserData(String username, byte[] picture) {
    this(null, username, picture, null);
  }

  public UserData(String username) {
    this(username, null);
  }

  public static final UserData DELETED = new UserData(null, null, null, null);
}
