package dev.tfkls.hikkei.model.user;

import java.time.LocalDateTime;

public record UserData(String username, byte[] picture, LocalDateTime joinedAt) {
  public static final UserData DELETED = new UserData(null, null, null);
}
