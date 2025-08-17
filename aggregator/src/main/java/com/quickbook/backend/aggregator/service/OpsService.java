package com.quickbook.backend.aggregator.service;

import com.quickbook.backend.aggregator.dto.OpsPageDto;
import com.quickbook.backend.aggregator.dto.OpsRequestDto;
import com.quickbook.backend.aggregator.dto.OpsResponse;
import com.quickbook.backend.aggregator.entity.postgres.Ops;
import com.quickbook.backend.aggregator.repo.postgres.OpsRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class OpsService {

    private final OpsRepo opsRepo;
    private final Integer pageSize;

    public OpsService(
            @Autowired
            OpsRepo opsRepo,
            @Value("${ops.fetch.page.size}")
            Integer pageSize) {
        this.opsRepo = opsRepo;
        this.pageSize = pageSize;
    }

    public OpsPageDto fetchOpsByDocId(String docId, Pageable page) {

        log.info("Page: {}", page.getPageNumber());
        Page<Ops> opsList = opsRepo.findByDocId(docId, page);

        return OpsPageDto.builder()
                .opsResponse(opsList
                        .getContent()
                        .stream()
                        .map(ops -> OpsResponse.builder()
                                .docId(ops.getDocId())
                                .userId(ops.getUserId())
                                .ts(ops.getTs())
                                .opType(ops.getOpType())
                                .position(ops.getPosition())
                                .content(ops.getContent())
                                .build()
                        ).toList())
                .page(opsList.nextPageable())
                .build();
    }

    public OpsPageDto fetchOpsByDocIdAndTs(String docId, Instant ts, Pageable page, Boolean history) {

        log.info("Page: {}", page.getPageNumber());
        Page<Ops> opsList = history ?
                opsRepo.findByDocIdAndTsLessThanEqual(docId, ts, page) :
                opsRepo.findByDocIdAndTsGreaterThan(docId, ts, page);

        return OpsPageDto.builder()
                .opsResponse(opsList
                        .getContent()
                        .stream()
                        .map(ops -> OpsResponse.builder()
                                .docId(ops.getDocId())
                                .userId(ops.getUserId())
                                .ts(ops.getTs())
                                .opType(ops.getOpType())
                                .position(ops.getPosition())
                                .content(ops.getContent())
                                .build()
                        ).toList())
                .page(opsList.nextPageable())
                .build();
    }

    public OpsPageDto fetchOpsByDocIdAndUserId(String docId, String userId, Pageable page) {
        Page<Ops> opsList = opsRepo.findByDocIdAndUserId(docId, userId, page);

        return OpsPageDto.builder()
                .opsResponse(opsList
                        .getContent()
                        .stream()
                        .map(ops -> OpsResponse.builder()
                                .docId(ops.getDocId())
                                .userId(ops.getUserId())
                                .opType(ops.getOpType())
                                .ts(ops.getTs())
                                .position(ops.getPosition())
                                .content(ops.getContent())
                                .build()
                        ).toList())
                .page(opsList.nextPageable())
                .build();
    }

    public List<String> fetchUsersByDocId(String docId) {
        return opsRepo.findDistinctUserIdsByDocId(docId);
    }

    public void store(String docId, String userId, List<OpsRequestDto> opsRequestDto) {

        List<Ops> ops = new ArrayList<>();
        for (OpsRequestDto requestDto : opsRequestDto) {
            ops.add(Ops.builder()
                    .docId(docId)
                    .userId(userId)
                    .ts(Instant.now().truncatedTo(ChronoUnit.MILLIS))
                    .opType(requestDto.getOpType())
                    .content(requestDto.getContent())
                    .position(requestDto.getPosition())
                    .build());

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                log.error("Error waiting for next ops");
            }
        }
        opsRepo.saveAll(ops);
    }

    public void delete(String docId, String userId) {

        Pageable page = PageRequest.of(0, 100, Sort.by("ts").ascending());
        while (page.isPaged()) {
            Page<Ops> batch = opsRepo.findByDocIdAndUserId(docId, userId, page);
            page = batch.nextPageable();
            opsRepo.deleteAll(batch.getContent());
        }
    }

}
