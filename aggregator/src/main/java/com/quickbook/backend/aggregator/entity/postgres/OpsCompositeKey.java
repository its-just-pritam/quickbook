package com.quickbook.backend.aggregator.entity.postgres;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class OpsCompositeKey implements Serializable {

    private String docId;
    private String userId;
    private Instant ts;

    public OpsCompositeKey() {}

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        if(obj == null || getClass() != obj.getClass()) return false;
        OpsCompositeKey that = (OpsCompositeKey) obj;
        return Objects.equals(docId, that.docId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(ts, that.ts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId, userId, ts);
    }
}
