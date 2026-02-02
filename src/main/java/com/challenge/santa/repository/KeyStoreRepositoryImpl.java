package com.challenge.santa.repository;

import com.challenge.santa.document.keystore.FileObj;
import com.challenge.santa.document.keystore.KeyStore;
import com.challenge.santa.persistence.KeyStoreReactiveRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class KeyStoreRepositoryImpl implements KeyStoreRepository {
  private final KeyStoreReactiveRepo repo;

  @Override
  public Mono<KeyStore> lock(FileObj fileObj) {
    return repo.acquireLock(fileObj);
  }

  @Override
  public Mono<Boolean> renew(FileObj fileObj) {
    return repo.renewLock(fileObj);
  }

  @Override
  public Mono<KeyStore> release(FileObj fileObj, String eTag) {
    return repo.releaseLock(fileObj, eTag);
  }

  @Override
  public Mono<KeyStore> release(FileObj fileObj) {
    return repo.releaseLock(fileObj, null);
  }
}
