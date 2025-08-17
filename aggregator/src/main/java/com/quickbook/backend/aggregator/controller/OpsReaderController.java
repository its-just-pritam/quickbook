package com.quickbook.backend.aggregator.controller;

import com.quickbook.backend.aggregator.dto.OpsPageDto;
import com.quickbook.backend.aggregator.dto.OpsResponse;
import com.quickbook.backend.aggregator.entity.mongo.SessionDocument;
import com.quickbook.backend.aggregator.repo.mongo.SessionDocumentRepo;
import com.quickbook.backend.aggregator.service.OpsService;
import com.quickbook.backend.aggregator.utils.AggregatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/ops")
public class OpsReaderController {

    private final OpsService opsService;
    private final Integer streamDelayInSeconds;
    private final AggregatorUtil aggregatorUtil;
    private final SessionDocumentRepo sessionDocumentRepo;

    public OpsReaderController(
            @Autowired
            OpsService opsService,
            @Value("${ops.stream.delay.milli}")
            Integer streamDelayInSeconds,
            @Autowired
            AggregatorUtil aggregatorUtil,
            @Autowired
            SessionDocumentRepo sessionDocumentRepo) {
        this.opsService = opsService;
        this.streamDelayInSeconds = streamDelayInSeconds;
        this.aggregatorUtil = aggregatorUtil;
        this.sessionDocumentRepo = sessionDocumentRepo;
    }

    @GetMapping(value = "/{docId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByDocId(@PathVariable String docId) {

        Pageable page = PageRequest.of(0, 100, Sort.by("ts").ascending());
        List<OpsResponse> response = new ArrayList<>();
        while (page.isPaged()) {
            OpsPageDto batch = opsService.fetchOpsByDocId(docId, page);
            page = batch.getPage();
            response.addAll(batch.getOpsResponse());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{docId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<OpsResponse> streamByDocId(@PathVariable String docId) {

        AtomicReference<Pageable> page = new AtomicReference<>(PageRequest.of(0, 100, Sort.by("ts").ascending()));
        Mono<OpsPageDto> initialOpsResponseMono = getOpsResponseMono(docId, page.get());

        Flux<OpsPageDto> allOpsResponses = initialOpsResponseMono.expand(
                previousOpsResponse -> {
                    if(previousOpsResponse.getPage().isPaged()) {
                        page.set(previousOpsResponse.getPage());
                        return getOpsResponseMono(docId, page.get())
                                .delayElement(Duration.ofMillis(streamDelayInSeconds));
                    }
                    else return Mono.empty();

                }
        );

        return allOpsResponses
                .filter(opsPageDto -> !opsPageDto.getOpsResponse().isEmpty())
                .flatMapIterable(OpsPageDto::getOpsResponse);
    }

    private Mono<OpsPageDto> getOpsResponseMono(String docId, Pageable page) {
        log.info("Processing getOpsResponseMono for docId: {}", docId);
        return Mono.defer(() -> Mono.fromCallable(
                () -> opsService.fetchOpsByDocId(docId, page)
        )).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping(value = "/{docId}/user", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByDocIdAndUserId(@PathVariable String docId) {
        return ResponseEntity.ok(opsService.fetchUsersByDocId(docId));
    }

    @GetMapping(value = "/{docId}/user/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByDocIdAndUserId(@PathVariable String docId, @PathVariable String userId) {

        Pageable page = PageRequest.of(0, 100, Sort.by("ts").ascending());
        List<OpsResponse> response = new ArrayList<>();
        while (page.isPaged()) {
            OpsPageDto batch = opsService.fetchOpsByDocIdAndUserId(docId, userId, page);
            page = batch.getPage();
            response.addAll(batch.getOpsResponse());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{docId}/document", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> getDocumentByDocId(
            @PathVariable String docId,
            @RequestParam(required = false) Instant ts,
            @RequestParam(defaultValue = "false") Boolean cached) {

        if(cached) {
            Optional<SessionDocument> sessionDocument = sessionDocumentRepo.findByDocId(docId);
            if(sessionDocument.isPresent() && sessionDocument.get().getContent() != null) {
                log.info("Invoking cached document for docId {}", docId);
                return ResponseEntity.ok(sessionDocument.get().getContent());
            }
        }

        ts = Optional.ofNullable(ts).orElse(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        Pageable page = PageRequest.of(0, 100, Sort.by("ts").ascending());

        List<OpsResponse> response = new ArrayList<>();
        while (page.isPaged()) {
            OpsPageDto batch = opsService.fetchOpsByDocIdAndTs(docId, ts, page, true);
            page = batch.getPage();
            response.addAll(batch.getOpsResponse());
        }

        String document = aggregatorUtil.merge(response, null);
        return ResponseEntity.ok(document);
    }

}
