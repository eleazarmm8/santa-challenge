package com.challenge.santa.repository;

import com.challenge.santa.document.sqsevent.SqsEventLog;
import com.challenge.santa.persistence.SqsEventLogReactiveRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SqsEventLogRepositoryImpl implements SqsEventLogRepository {
  private final SqsEventLogReactiveRepo repository;

  @Override
  public Flux<SqsEventLog> findAllByBucketName(String bucketName, Pageable pageable) {
    return repository.findAllByBucketName(bucketName, pageable);
  }

  @Override
  public Mono<Long> countByBucketName(String bucketName) {
    return repository.countByBucketName(bucketName);
  }

  @Override
  public Mono<SqsEventLog> insertEvent(SqsEventLog eventLog) {
    return repository.insert(eventLog)
          // We don't care about duplicates, catch and continue
          .onErrorResume(DuplicateKeyException.class, e -> {
            log.info("Duplicated Event caught. Discarding");
            // Return the object as-is
            return Mono.just(eventLog);
          });
  }
}
