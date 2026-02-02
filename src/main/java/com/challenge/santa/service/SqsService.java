package com.challenge.santa.service;

import com.challenge.santa.document.sqsevent.SqsEventLog;
import com.challenge.santa.dto.event.SqsEventDTO;
import com.challenge.santa.exception.SantaException;
import com.challenge.santa.properties.AwsProperties;
import com.challenge.santa.properties.SqsProperties;
import com.challenge.santa.repository.SqsEventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsService {
  private final AwsProperties awsProperties;
  private final SqsProperties sqsProperties;
  private final SqsEventLogRepository repository;

  public Mono<SqsEventLog> sendEvent(SqsEventDTO event, String eTag) {
    String messageBody = String.format(
          "File with key=%s was concluded successfully. OperationType=%s",
          event.getObjectKey(), event.getEventType());

    // Create the request message
    SendMessageRequest request = SendMessageRequest.builder()
          .queueUrl(sqsProperties.getQueueUrl())
          .messageBody(messageBody)
          .messageGroupId("file-uploads-group")
          .messageDeduplicationId(hashedKey(eTag, event.getObjectKey(), event.getBucketName()))
          .messageAttributes(event.toMap()) // the whole event data travels as Attributes
          .build();

    // Another AutoCloseable wrapped into a try block
    try (SqsAsyncClient client = awsProperties.sqsAsyncClient()) {
      return Mono.fromFuture(() -> client.sendMessage(request))
            // Process the response to include the eTag and the id into our log
            .flatMap(response -> {
              SqsEventLog eventLog = event.toDocument()
                    .withId(response.messageId())
                    .withETag(eTag);

              return repository.insertEvent(eventLog);
            })
            .doOnSuccess(resp -> log.info("Message sent with id {}", resp.getId()))
            .doOnError(error -> log.info("An error occurred during message delivery", error))
            // Catch and rethrow in case of error
            .onErrorResume(AwsServiceException.class, e ->
                  Mono.error(new SantaException(e)));
    }
  }

  @SneakyThrows
  private String hashedKey(String eTag, String key, String bucket) {
    // I ignore the exception, SHA-256 and the Security interface is available in every version of java since 9
    final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
    final byte[] hash = digest.digest(
          String.join("-", eTag, key, bucket).getBytes(StandardCharsets.UTF_8));
    return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
  }
}
