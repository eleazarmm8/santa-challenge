package com.challenge.santa.properties;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.time.Duration;
import java.util.Arrays;

@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
  private final String region;
  private final String accessKey;
  private final String secretKey;

  private StaticCredentialsProvider getCredentials() {
    return StaticCredentialsProvider.create(
          AwsBasicCredentials.create(accessKey, secretKey)
    );
  }

  /**
   * This method ensures that upon startup, no incorrect region is given to avoid unnecessary application locks
   * and waste bandwidth trying to reach invalid regions.
   * This totally blocks application startup.
   *
   * @throws IllegalArgumentException if the given region is not one of the known and valid ones
   */
  private void validateRegion() {
    boolean validRegion = Region.regions().stream()
          .anyMatch(r -> r.id().equals(region));

    if (!validRegion) {
      throw new IllegalArgumentException("Unknown region " + region
            + " please provide one of the valid regions known by AWS.\nValid regions are: "
            + Arrays.toString(Region.regions().toArray()));
    }
  }

  @Bean
  public S3AsyncClient s3AsyncClient() {
    validateRegion();
    return S3AsyncClient.builder()
          .region(Region.of(region))
          .credentialsProvider(getCredentials())
          .build();
  }

  @Bean
  public SqsAsyncClient sqsAsyncClient() {
    validateRegion();
    return SqsAsyncClient.builder()
          .region(Region.of(region))
          .credentialsProvider(getCredentials())
          .build();
  }
}
