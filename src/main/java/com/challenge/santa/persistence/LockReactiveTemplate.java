package com.challenge.santa.persistence;

import com.challenge.santa.document.keystore.FileObj;
import com.challenge.santa.document.keystore.KeyStore;
import reactor.core.publisher.Mono;

public interface LockReactiveTemplate {
  Mono<KeyStore> acquireLock(FileObj fileObj);
  Mono<Boolean> renewLock(FileObj fileObj);
  Mono<KeyStore> releaseLock(FileObj fileObj, String eTag);
}
