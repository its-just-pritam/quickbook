package com.quickbook.backend.aggregator.job;

import com.quickbook.backend.aggregator.dto.OpsPageDto;
import com.quickbook.backend.aggregator.dto.OpsResponse;
import com.quickbook.backend.aggregator.entity.mongo.SessionDocument;
import com.quickbook.backend.aggregator.repo.mongo.SessionDocumentRepo;
import com.quickbook.backend.aggregator.repo.postgres.OpsRepo;
import com.quickbook.backend.aggregator.service.OpsService;
import com.quickbook.backend.aggregator.utils.AggregatorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
@Component
public class AggregatorJob {

    private final SessionDocumentRepo sessionDocumentRepo;
    private final BlockingQueue<String> queue;
    private final OpsRepo opsRepo;
    private final OpsService opsService;
    private final AggregatorUtil aggregatorUtil;

    public AggregatorJob(
            @Autowired
            SessionDocumentRepo sessionDocumentRepo,
            @Autowired
            OpsRepo opsRepo,
            @Autowired
            OpsService opsService,
            @Autowired
            AggregatorUtil aggregatorUtil) {
        this.sessionDocumentRepo = sessionDocumentRepo;
        this.opsRepo = opsRepo;
        this.opsService = opsService;
        this.aggregatorUtil = aggregatorUtil;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void produce(String docId) {
        try {
            queue.put(docId);
            log.info("Produced: {}", docId);
        } catch (InterruptedException e) {
            log.error("Failed to produce: {}", docId, e);
        }
    }

    @Scheduled(fixedRate = 30000)
    public void produce() throws InterruptedException {
        List<String> docs = opsRepo.findDistinctDoc();
        for(String docId: docs) {
            queue.put(docId);
            log.info("Produced: {}", docId);
        }
    }

    @Scheduled(fixedRate = 5000)
    public void insertDocEveryMinute() throws InterruptedException {

        String docId = queue.take();
        log.info("Consumed: {}", docId);

        try {
            Optional<SessionDocument> sessionDocument = sessionDocumentRepo.findByDocId(docId);

            if(sessionDocument.isEmpty()) {
                Pageable page = PageRequest.of(0, 100, Sort.by("ts").ascending());
                List<OpsResponse> response = new ArrayList<>();

                while (page.isPaged()) {
                    OpsPageDto batch = opsService.fetchOpsByDocIdAndTs(docId, Instant.now().truncatedTo(ChronoUnit.MILLIS), page, true);
                    page = batch.getPage();
                    response.addAll(batch.getOpsResponse());
                }

                if(response.isEmpty()) {
                    log.info("Skipping doc {}, no ops found", docId);
                    return;
                }

                String document = aggregatorUtil.merge(response, null);
                sessionDocumentRepo.save(SessionDocument.builder()
                        .id(docId)
                        .docId(docId)
                        .name(response.getLast().getUserId())
                        .content(document)
                        .ts(response.getLast().getTs())
                        .createdAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                        .updatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                        .build());
                log.info("Inserted: {}", response.getLast().getDocId());

            } else if(sessionDocument.get().getUpdatedAt().toEpochMilli() >= Instant.now().truncatedTo(ChronoUnit.MILLIS).minusSeconds(60).toEpochMilli()) {
                log.info("Skipping doc {}", docId);

            } else {
                Pageable page = PageRequest.of(0, 100, Sort.by("ts").ascending());
                List<OpsResponse> response = new ArrayList<>();

                while (page.isPaged()) {
                    OpsPageDto batch = opsService.fetchOpsByDocIdAndTs(docId, sessionDocument.get().getTs(), page, false);
                    page = batch.getPage();
                    response.addAll(batch.getOpsResponse());
                }

                if(response.isEmpty()) {
                    log.info("Skipping doc {}, no recent ops found", docId);
                    return;
                }

                List<String> previousDocumentContent = new ArrayList<>(Arrays.asList(sessionDocument.get().getContent().split(" ")));
                String document = aggregatorUtil.merge(response, previousDocumentContent);

                sessionDocument.get().setContent(document);
                sessionDocument.get().setTs(response.getLast().getTs());
                sessionDocument.get().setName(response.getLast().getUserId());
                sessionDocument.get().setUpdatedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS));
                sessionDocumentRepo.save(sessionDocument.get());
                log.info("Updated: {}", response.getLast().getDocId());
            }
        } catch (Exception e) {
            log.error("Error in insertDocEveryMinute for doc {}", docId, e);
        }

    }
}
