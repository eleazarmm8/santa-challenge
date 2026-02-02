package com.challenge.santa.controller;

import com.challenge.santa.document.sqsevent.SqsEventLog;
import com.challenge.santa.dto.request.MetadataRequest;
import com.challenge.santa.dto.response.EventResponse;
import com.challenge.santa.service.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/s3_events")
@RequiredArgsConstructor
public class AwsController {
  private final OrchestratorService service;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public Mono<EventResponse> pushFileToBucket(
        @RequestPart("file") FilePart file,
        @RequestPart("metadata") @Valid MetadataRequest metadata) {
    return service.pushFileToBucket(file, metadata);
  }

  @GetMapping("{bucketName}")
  public Mono<Page<SqsEventLog>> getEventsForBucket(
        @PathVariable String bucketName,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    return service.getSqsEventLog(bucketName, page, size);
  }
}
