package com.challenge.santa.document.keystore;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class FileObj {
  private final String bucketName;
  private final String key;
}
