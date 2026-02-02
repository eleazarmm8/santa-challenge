package com.challenge.santa.document.keystore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "key_store")
@CompoundIndex(def = "{'fileObj.bucketName': 1, 'fileObj.key': 1}", unique = true)
public class KeyStore {
  @Id
  private String id;

  private FileObj fileObj;
  private String eTag;
  private LockStatus status;
  private Instant heartBeat;
}
