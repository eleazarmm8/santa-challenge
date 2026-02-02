package com.challenge.santa.handler;

import com.challenge.santa.exception.ExceptionDetail;
import com.challenge.santa.exception.SantaException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(SantaException.class)
  public ExceptionDetail handleSantaException(SantaException ex) {
    return new ExceptionDetail(ex.getCode(), ex.getStatus().value(), ex.getMessage());
  }
}
