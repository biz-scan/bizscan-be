package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SwotAnalyzeRequest {

    private Long storeId;
}