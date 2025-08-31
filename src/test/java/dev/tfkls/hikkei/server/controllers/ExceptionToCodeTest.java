package dev.tfkls.hikkei.server.controllers;

import static dev.tfkls.hikkei.server.controllers.Utils.exceptionToCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.tfkls.hikkei.model.exceptions.BadDataException;
import dev.tfkls.hikkei.model.exceptions.InternalErrorException;
import org.junit.jupiter.api.Test;

public class ExceptionToCodeTest {
  @Test
  public void BadDataExceptionTest() {
    assertEquals(400, exceptionToCode(new BadDataException("")));
  }

  @Test
  public void InternalErrorExceptionTest() {
    assertEquals(500, exceptionToCode(new InternalErrorException("")));
  }

  @Test
  public void OtherExceptionTest() {
    assertEquals(500, exceptionToCode(new Exception("")));
    assertEquals(500, exceptionToCode(new RuntimeException("")));
    assertEquals(500, exceptionToCode(null));
  }
}
