package com.quickbook.backend.aggregator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpsResponse {

    private String docId;
    private String userId;
    private Instant ts;
    private String opType;
    private Long position;
    private String content;

}
