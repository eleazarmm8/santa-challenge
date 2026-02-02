package com.challenge.santa.persistence;

import com.challenge.santa.document.keystore.FileObj;
import com.challenge.santa.document.keystore.KeyStore;
import com.challenge.santa.document.keystore.LockStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.utils.StringUtils;

import java.time.Duration;
import java.time.Instant;

@Repository
@RequiredArgsConstructor
public class LockReactiveTemplateImpl implements LockReactiveTemplate {
  private final ReactiveMongoTemplate mongoTemplate;
  private final Duration ttl = Duration.ofSeconds(30);

  @Override
  public Mono<KeyStore> acquireLock(FileObj fileObj) {
    Instant currentTime = Instant.now();
    Query query = Query.query(Criteria
          .where("fileObj").is(fileObj)
          .orOperator(
                Criteria.where("status").is(LockStatus.AVAILABLE),
                Criteria.where("heartBeat").lt(currentTime)
          ));

    Update update = new Update()
          .set("status", LockStatus.LOCKED)
          .set("heartBeat", currentTime.plus(ttl))
          .setOnInsert("fileObj", fileObj);

    FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);
    return mongoTemplate
          .findAndModify(query, update, options, KeyStore.class)
          .onErrorResume(e-> Mono.empty());
  }

  @Override
  public Mono<Boolean> renewLock(FileObj fileObj) {
    Query query = Query.query(Criteria.where("fileObj").is(fileObj)
          .and("status").is(LockStatus.LOCKED));

    Update update = new Update()
          .set("heartBeat", Instant.now().plus(ttl));

    return mongoTemplate.updateFirst(query, update, KeyStore.class)
          .map(result -> result.getModifiedCount() > 0);
  }

  @Override
  public Mono<KeyStore> releaseLock(FileObj fileObj, String eTag) {
    Query query = Query.query(Criteria.where("fileObj").is(fileObj));
    Update update = new Update()
          .set("status", LockStatus.AVAILABLE)
          .unset("heartBeat"); // No more use for the heartBeat.
    if (StringUtils.isNotBlank(eTag)) {
      update.set("eTag", eTag);
    }

    FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
    return mongoTemplate.findAndModify(query, update, options, KeyStore.class);
  }
}
