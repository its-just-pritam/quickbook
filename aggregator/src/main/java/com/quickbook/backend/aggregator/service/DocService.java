package com.quickbook.backend.aggregator.service;

import com.quickbook.backend.aggregator.entity.mongo.SessionDocument;
import com.quickbook.backend.aggregator.repo.mongo.SessionDocumentRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocService {

    private final SessionDocumentRepo sessionDocumentRepo;

    public DocService(SessionDocumentRepo sessionDocumentRepo) {
        this.sessionDocumentRepo = sessionDocumentRepo;
    }

    public void delete(String docId) {
        sessionDocumentRepo.deleteById(docId);
    }

    public String get(String docId) {
        return sessionDocumentRepo.findById(docId).map(SessionDocument::getContent).orElse(null);
    }
}
