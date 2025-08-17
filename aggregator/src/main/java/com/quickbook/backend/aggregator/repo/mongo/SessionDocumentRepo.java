package com.quickbook.backend.aggregator.repo.mongo;

import com.quickbook.backend.aggregator.entity.mongo.SessionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SessionDocumentRepo extends MongoRepository<SessionDocument, String> {

    Optional<SessionDocument> findByDocId(String docId);
}
