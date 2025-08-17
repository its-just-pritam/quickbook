package com.quickbook.backend.aggregator.entity.postgres;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "ops")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@IdClass(OpsCompositeKey.class)
public class Ops {

    @Id
    @Column(name = "doc_id", nullable = false)
    private String docId;

    @Id
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Id
    @Column(name = "ts", nullable = false)
    private Instant ts;

    @Column(name = "op_type")
    private String opType;

    @Column(name = "position")
    private Long position;

    @Column(name = "content")
    private String content;

}
