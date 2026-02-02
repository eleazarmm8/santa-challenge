package com.challenge.santa.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

@Getter
public class SantaException extends RuntimeException { // Generic exception
  private final HttpStatus status;
  private final String code;

  public SantaException(String message, HttpStatus status, String code) {
    super(message);
    this.status = status;
    this.code = code;
  }

  public SantaException(AwsServiceException serviceException) {
    super(serviceException.getMessage());
    this.status = HttpStatus.valueOf(serviceException.awsErrorDetails().sdkHttpResponse().statusCode());
    this.code = serviceException.awsErrorDetails().errorCode();
  }
}
