package com.quickbook.backend.aggregator.dto;

import io.netty.util.internal.StringUtil;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.coyote.BadRequestException;

import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpsRequestDto {

    private String opType;
    private Long position;
    private String content;

    public void validate() throws BadRequestException {
        if(!(Objects.equals(this.opType, "insert") || Objects.equals(this.opType, "delete")))
            throw new BadRequestException(this.opType + " opType not supported");
        if(this.position < 0)
            throw new BadRequestException(this.position + " position not supported");
        if(StringUtil.isNullOrEmpty(this.content))
            throw new BadRequestException("content is mandatory");
    }

}
