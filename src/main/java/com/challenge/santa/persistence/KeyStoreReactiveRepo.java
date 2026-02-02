package com.challenge.santa.persistence;

import com.challenge.santa.document.keystore.KeyStore;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface KeyStoreReactiveRepo extends ReactiveMongoRepository<KeyStore, String>, LockReactiveTemplate {
}
