package dev.tfkls.hikkei.model.user;

public class BadDataException extends RuntimeException {
  public BadDataException(String message) {
    super(message);
  }
}
