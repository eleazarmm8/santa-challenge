package com.challenge.santa.exception;

import org.springframework.http.HttpStatus;

public class LockedFileException extends SantaException {
  private final static String CODE = "File in use";
  private final static String MESSAGE = "File is currently being created or updated, please try again later";

  public LockedFileException() {
    super(MESSAGE, HttpStatus.LOCKED, CODE);
  }
}
