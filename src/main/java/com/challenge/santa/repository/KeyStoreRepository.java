package com.challenge.santa.repository;

import com.challenge.santa.document.keystore.FileObj;
import com.challenge.santa.document.keystore.KeyStore;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface KeyStoreRepository {
  Mono<KeyStore> lock(FileObj fileObj);
  Mono<Boolean> renew(FileObj fileObj);
  Mono<KeyStore> release(FileObj fileObj, String eTag);
  Mono<KeyStore> release(FileObj fileObj);
}
