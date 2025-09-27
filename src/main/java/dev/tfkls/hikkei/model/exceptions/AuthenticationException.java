package dev.tfkls.hikkei.model.exceptions;

public sealed class AuthenticationException extends RuntimeException
    permits AuthenticationSuccessException, AuthenticationFailureException {
  public AuthenticationException(String message) {
    super(message);
  }

  public AuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
