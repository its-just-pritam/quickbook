package com.quickbook.backend.aggregator.controller;

import com.quickbook.backend.aggregator.service.DocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Slf4j
@RestController
@RequestMapping("/doc")
public class DocController {

    private final DocService docService;

    public DocController(
            @Autowired
            DocService docService) {
        this.docService = docService;
    }

    @GetMapping(value = "/{docId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> getDocumentByDocId(
            @PathVariable String docId,
            @RequestParam(required = false) Instant ts) {

        return ResponseEntity.ok(docService.get(docId));
    }

    @DeleteMapping(value = "/{docId}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> deleteDoc(@PathVariable String docId) {

        docService.delete(docId);
        return ResponseEntity.noContent().build();
    }
}
