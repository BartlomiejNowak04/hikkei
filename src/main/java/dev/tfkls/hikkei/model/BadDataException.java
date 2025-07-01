package dev.tfkls.hikkei.model;

public class BadDataException extends RuntimeException {
  public BadDataException(String message) {
    super(message);
  }
}
