package com.umc9th.bizscan.domain.aiAnalysis.converter;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.SwotCallbackReqDTO;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import com.umc9th.bizscan.domain.aiAnalysis.enums.SwotType;

public class SwotConverter {
    public static Swot toSwot(SwotCallbackReqDTO.SwotItem dto, Analysis analysis) {
        return Swot.builder()
                .analysis(analysis)
                .type(SwotType.valueOf(dto.type().toUpperCase())) // "S" -> SwotType.S
                .keyword(dto.keyword())
                .description(dto.description())
                .diagnosis(dto.diagnosis())
                .build();
    }
}
