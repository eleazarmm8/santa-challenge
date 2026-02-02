package com.challenge.santa.document.sqsevent;

import com.challenge.santa.dto.event.EventType;
import lombok.Builder;
import lombok.Data;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@With
@Document(collection = "sqs_events_log")
@CompoundIndexes({
      @CompoundIndex(name = "bucket_time_idx", def = "{'bucketName': 1, 'eventTime': -1}")
})
public class SqsEventLog {
  @Id
  private String id;
  private String eTag;
  private String bucketName;
  private String objectKey;
  private EventType eventType;
  private Instant eventTime;
  private Long objectSize;
}
