package dev.tfkls.hikkei.model.user;

public class BadHandleException extends RuntimeException {
  public BadHandleException(String message) {
    super(message);
  }
}
