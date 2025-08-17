package com.quickbook.backend.aggregator.entity.mongo;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Document(collection = "session_document")
public class SessionDocument {

    @Id
    private String id;
    private String docId;
    private String name;
    private String content;
    private Instant ts;
    private Instant createdAt;
    private Instant updatedAt;

}
