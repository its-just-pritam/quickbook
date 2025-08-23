package com.quickbook.backend.aggregator.utils;

import com.quickbook.backend.aggregator.dto.OpsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.quickbook.backend.aggregator.Constant.DELETE;
import static com.quickbook.backend.aggregator.Constant.INSERT;

@Slf4j
@Component
public class AggregatorUtil {

    public String merge(List<OpsResponse> operations, List<String> document) {

        document = Optional.ofNullable(document).orElse(new ArrayList<>());

        List<OpsResponse> transformed = new ArrayList<>();
        for (OpsResponse op : operations) {
            OpsResponse transformedOp = transform(op, transformed);
            apply(transformedOp, document);
            transformed.add(transformedOp);
        }

        return String.join(" ", document);
    }

    private OpsResponse transform(OpsResponse op, List<OpsResponse> priorOps) {
        long pos = op.getPosition();

        for (OpsResponse prev : priorOps) {
            if (INSERT.equals(prev.getOpType())) {
                if (prev.getPosition() <= pos)
                    pos++;
            } else if (DELETE.equals(prev.getOpType())) {
                if (prev.getPosition() < pos)
                    pos--;
                else if (prev.getPosition().equals(pos) && DELETE.equals(op.getOpType()))
                    return null;
            }
        }

        return OpsResponse.builder()
                .docId(op.getDocId())
                .userId(op.getUserId())
                .ts(op.getTs())
                .opType(op.getOpType())
                .position(pos)
                .content(op.getContent())
                .build();
    }

    private void apply(OpsResponse op, List<String> document) {

        if (op == null) return;
        int pos = Math.toIntExact(op.getPosition());

        if (INSERT.equals(op.getOpType())) {
            if (pos <= document.size())
                document.add(pos, op.getContent());
            else
                document.add(op.getContent());
        } else if (DELETE.equals(op.getOpType()) && pos < document.size())
            document.remove(pos);
    }

}
