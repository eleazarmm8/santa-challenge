package com.challenge.santa.service;

import com.challenge.santa.document.keystore.FileObj;
import com.challenge.santa.document.sqsevent.SqsEventLog;
import com.challenge.santa.dto.event.EventType;
import com.challenge.santa.dto.event.SqsEventDTO;
import com.challenge.santa.dto.request.MetadataRequest;
import com.challenge.santa.dto.response.EventResponse;
import com.challenge.santa.dto.response.PutObjectWithSize;
import com.challenge.santa.exception.LockedFileException;
import com.challenge.santa.repository.KeyStoreRepository;
import com.challenge.santa.repository.SqsEventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrchestratorService {
  private final S3Service s3Service;
  private final SqsService sqsService;
  private final KeyStoreRepository keyStoreRepository;
  private final SqsEventLogRepository eventLogRepository;

  public Mono<EventResponse> pushFileToBucket(FilePart file, MetadataRequest request) {
    // Initialize the event for later reference. Keep it as a builder for easier handling
    SqsEventDTO.SqsEventDTOBuilder eventBuilder = SqsEventDTO.builder()
          .bucketName(request.getBucketName())
          .objectKey(request.getKey());
    FileObj fileObj = new FileObj(request.getBucketName(), request.getKey());

    return keyStoreRepository.lock(fileObj) // acquire lock
          // failed
          .switchIfEmpty(Mono.error(new LockedFileException()))
          // acquired
          .flatMap(keyStore -> {
            // Heartbeat generation
            Flux<Boolean> heartBeatStream = Flux.interval(Duration.ofSeconds(15))
                  .flatMap(i -> keyStoreRepository.renew(fileObj))
                  .doOnNext(renew -> {
                    if (!renew) log.warn("Lock lost during upload! This might cause terrible problems");
                  });

            // eventType is calculated depending on the eTag.
            eventBuilder.eventType(keyStore.getETag() != null ? EventType.OBJECT_UPDATED : EventType.OBJECT_CREATED);

            // Queue the file upload
            Mono<PutObjectWithSize> upload = s3Service.push(fileObj, file);

            // Merge both streams
            return Flux.merge(upload, heartBeatStream)
                  // Filter out only the response from "upload", ending the heartBeat in the process
                  .filter(obj -> obj instanceof PutObjectWithSize)
                  .cast(PutObjectWithSize.class)
                  // Take the only result
                  .next()
                  // We reached this far, let's send the event!
                  .flatMap(response -> {
                    eventBuilder.objectSize(response.getContentLength());
                    // Clear eTag's extra double quotes
                    String eTag = response.getPutObject().eTag().replaceAll("\"", "");
                    // Release the lock, we are done with it.
                    return keyStoreRepository.release(fileObj, eTag);
                  })
                  // Save the event on our log
                  .flatMap(finalResponse -> {
                    eventBuilder.eventTime(Instant.now());
                    return sqsService.sendEvent(eventBuilder.build(), finalResponse.getETag())
                          .map(EventResponse::new);
                  });
          }).onErrorResume(err ->
                // In case of an unexpected error at any point, release the lock anyway
                keyStoreRepository.release(fileObj)
                      .then(Mono.error(err))
          );
  }

  public Mono<Page<SqsEventLog>> getSqsEventLog(String bucketName, int page, int size) {
    // We enforce the ordering of the pageable request
    Pageable pageable = PageRequest.of(page, size, Sort.by("eventTime").descending());

    // Get every event for the given bucket
    return eventLogRepository.findAllByBucketName(bucketName, pageable)
          // Collect the result
          .collectList()
          // Add the total count for the page result
          .zipWith(eventLogRepository.countByBucketName(bucketName))
          // Map it to a Page object
          .map(tuple ->
                new PageImpl<>(tuple.getT1(), pageable, tuple.getT2())
          );
  }
}
