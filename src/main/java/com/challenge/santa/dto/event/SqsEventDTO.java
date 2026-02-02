package com.challenge.santa.dto.event;

import com.challenge.santa.document.sqsevent.SqsEventLog;
import lombok.Builder;
import lombok.Data;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Data
@Builder
public class SqsEventDTO {
  private String bucketName;
  private String objectKey;
  private EventType eventType;
  private Instant eventTime;
  private Long objectSize;

  private final Map<String, Supplier<Object>> fieldsMap = Map.ofEntries(
        new AbstractMap.SimpleImmutableEntry<>("bucketName", this::getBucketName),
        new AbstractMap.SimpleImmutableEntry<>("objectKey", this::getObjectKey),
        new AbstractMap.SimpleImmutableEntry<>("eventType", this::getEventType),
        new AbstractMap.SimpleImmutableEntry<>("eventTime", this::getEventTime),
        new AbstractMap.SimpleImmutableEntry<>("objectSize", this::getObjectSize)
  );

  /**
   * This takes the Supplier for each function which ensures the value is always the latest without needing
   * to make an operation for every single property of the object.
   *
   * @return The Attribute map for the SQS Event
   */
  public Map<String, MessageAttributeValue> toMap() {
    Map<String, MessageAttributeValue> eventsMap = new HashMap<>();
    for (Map.Entry<String, Supplier<Object>> entry : fieldsMap.entrySet()) {
      eventsMap.computeIfAbsent(entry.getKey(), (k) -> MessageAttributeValue.builder()
            .dataType("String")
            .stringValue(getValueFromSupplier(entry.getValue()))
            .build());
    }
    return eventsMap;
  }

  public SqsEventLog toDocument() {
    return SqsEventLog.builder()
          .eventType(this.eventType)
          .eventTime(this.eventTime)
          .objectKey(this.objectKey)
          .objectSize(this.objectSize)
          .bucketName(this.bucketName)
          .build();
  }

  private String getValueFromSupplier(Supplier<Object> supplier) {
    Object r = supplier.get();
    if (r instanceof EventType) {
      return ((EventType) r).name();
    }
    if (r instanceof Instant) {
      return ((Instant) r).toString();
    }
    return String.valueOf(r);
  }
}
