package com.quickbook.backend.aggregator.controller;

import com.quickbook.backend.aggregator.dto.OpsRequestDto;
import com.quickbook.backend.aggregator.job.AggregatorJob;
import com.quickbook.backend.aggregator.service.OpsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/ops")
public class OpsWriterController {

    private final OpsService opsService;
    private final AggregatorJob aggregatorJob;

    public OpsWriterController(
            @Autowired
            OpsService opsService,
            @Autowired
            AggregatorJob aggregatorJob) {
        this.opsService = opsService;
        this.aggregatorJob = aggregatorJob;
    }

    @PostMapping(value = "/{docId}/user/{userId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> storeByDocIdAndUserId(
            @PathVariable
            String docId,
            @PathVariable
            String userId,
            @RequestBody
            List<OpsRequestDto> opsRequestDto) {

        for(OpsRequestDto requestDto: opsRequestDto) {
            try {
                requestDto.validate();
            } catch (BadRequestException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        opsService.store(docId, userId, opsRequestDto);
        aggregatorJob.produce(docId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{docId}/user/{userId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> deleteByDocIdAndUserId(
            @PathVariable
            String docId,
            @PathVariable
            String userId) {

        opsService.delete(docId, userId);
        return ResponseEntity.noContent().build();
    }



}
