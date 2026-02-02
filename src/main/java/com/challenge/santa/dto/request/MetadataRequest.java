package com.challenge.santa.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@AllArgsConstructor
public final class MetadataRequest {
  @NotBlank
  private final String key;
  @NotBlank
  private final String bucketName;
  private final String description;
}
