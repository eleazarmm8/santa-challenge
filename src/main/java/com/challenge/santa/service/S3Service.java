package com.challenge.santa.service;

import com.challenge.santa.document.keystore.FileObj;
import com.challenge.santa.dto.response.PutObjectWithSize;
import com.challenge.santa.exception.SantaException;
import com.challenge.santa.properties.AwsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class S3Service {
  private final AwsProperties awsProperties;

  public Mono<PutObjectWithSize> push(FileObj fileObj, FilePart file) {
    // Turn file content into a DataBuffer
    return DataBufferUtils.join(file.content())
          .flatMap(dataBuffer -> {
            // Get the size
            long size = dataBuffer.readableByteCount();

            // Generate the S3 request object
            PutObjectRequest request = PutObjectRequest.builder()
                  .bucket(fileObj.getBucketName())
                  .key(fileObj.getKey())
                  .contentLength(size)
                  .contentType(Objects.toString(file.headers().getContentType()))
                  .build();

            // Enveloped into a try block since is AutoCloseable
            try (S3AsyncClient client = awsProperties.s3AsyncClient()) {
              return Mono.fromFuture(() -> client.putObject(request,
                          AsyncRequestBody.fromByteBuffer(dataBuffer.asByteBuffer())))
                    // We include the PutObjectResponse along with the Size for the future event
                    .map(r -> new PutObjectWithSize(r, size))
                    // In case any error happened, let's parse it accordingly
                    .onErrorResume(AwsServiceException.class,
                          e -> Mono.error(new SantaException(e)));
            }
          });
  }
}
