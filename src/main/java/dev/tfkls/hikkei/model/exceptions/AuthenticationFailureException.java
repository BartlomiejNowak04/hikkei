package dev.tfkls.hikkei.model.exceptions;

public final class AuthenticationFailureException extends AuthenticationException {
  public AuthenticationFailureException(String message) {
    super(message);
  }

  public AuthenticationFailureException(String message, Throwable cause) {
    super(message, cause);
  }
}
