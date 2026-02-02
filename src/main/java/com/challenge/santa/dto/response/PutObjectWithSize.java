package com.challenge.santa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Getter
@AllArgsConstructor
public final class PutObjectWithSize {
  private final PutObjectResponse putObject;
  private final Long contentLength;
}
