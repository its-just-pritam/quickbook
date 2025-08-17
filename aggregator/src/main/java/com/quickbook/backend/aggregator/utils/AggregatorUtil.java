package com.quickbook.backend.aggregator.utils;

import com.quickbook.backend.aggregator.dto.OpsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class AggregatorUtil {

    public String merge(List<OpsResponse> response, List<String> document) {

        document = Optional.ofNullable(document).orElse(new ArrayList<>());

        for(OpsResponse opsResponse: response) {
            Long position = opsResponse.getPosition();
            if(Objects.equals(opsResponse.getOpType(), "insert")) {
                if(position < document.size())
                    document.add(Math.toIntExact(position), opsResponse.getContent());
                else {
                    while(document.size() <= position)
                        document.add("");
                    document.set(Math.toIntExact(position), opsResponse.getContent());
                }
            } else if (Objects.equals(opsResponse.getOpType(), "delete")) {
                if(position < document.size())
                    document.remove(Math.toIntExact(position));
            }
        }

        return String.join(" ", document);
    }

}
