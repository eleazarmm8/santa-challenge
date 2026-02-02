package com.challenge.santa.repository;

import com.challenge.santa.document.sqsevent.SqsEventLog;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SqsEventLogRepository {
  Flux<SqsEventLog> findAllByBucketName(String bucketName, Pageable pageable);
  Mono<Long> countByBucketName(String bucketName);
  Mono<SqsEventLog> insertEvent(SqsEventLog eventLog);
}
