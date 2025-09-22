package dev.tfkls.hikkei.model.permission;

public enum PermissionLevel {
  NO_ACCESS,
  UNTRUSTED,
  TRUSTED,
  MODERATOR,
  ADMIN,
  SUPER_ADMIN;

  public static boolean isLess(PermissionLevel lower, PermissionLevel higher) {
    return lower.compareTo(higher) < 0;
  }

  public static PermissionLevel max(PermissionLevel one, PermissionLevel two) {
    return isLess(one, two) ? two : one;
  }
}
