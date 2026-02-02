package com.challenge.santa.dto.response;

import com.challenge.santa.document.sqsevent.SqsEventLog;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;

import java.util.Map;

public class EventResponse extends ResponseEntity<Map<String, String>> {
  public EventResponse(@Nullable SqsEventLog body) {
    super(Map.of("eventId", body != null ? body.getId() : "none"), HttpStatus.OK);
  }
}
