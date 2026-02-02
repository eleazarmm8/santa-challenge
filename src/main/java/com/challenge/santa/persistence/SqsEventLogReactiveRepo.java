package com.challenge.santa.persistence;

import com.challenge.santa.document.sqsevent.SqsEventLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SqsEventLogReactiveRepo extends ReactiveMongoRepository<SqsEventLog, String> {
  Flux<SqsEventLog> findAllByBucketName(String bucketName, Pageable pageable);

  Mono<Long> countByBucketName(String bucketName);
}
