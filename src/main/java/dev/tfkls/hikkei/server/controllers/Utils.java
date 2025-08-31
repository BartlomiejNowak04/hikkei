package dev.tfkls.hikkei.server.controllers;

public class Utils {
  public static int exceptionToCode(Exception e) {
    if (e == null) return 500;
    return switch (e.getClass().getSimpleName()) {
      case "BadDataException", "DecodeException" -> 400;
      case "InternalErrorException" -> 500;
      default -> 500;
    };
  }
}
