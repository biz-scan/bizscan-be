package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SwotCallbackReqDTO {

    /**
     * 메인 콜백 DTO: Python의 CallbackResponse 구조와 매핑
     */
    public record SwotCallbackDTO(
            boolean isSuccess,
            String code,
            String message,
            @JsonProperty("request_id") String requestId,
            String status,
            SwotReqDTO result
    ) {}

    /**
     * Python의 SWOTCallbackResponse 매핑
     */
    public record SwotReqDTO(
            CatchphraseResult catchphrase,
            SwotResult swot
    ) {}

    /**
     * Python의 CatchphraseResponse 매핑
     */
    public record CatchphraseResult(
            String catchphrase
    ) {}

    /**
     * Python의 SWOTResponse 매핑
     */
    public record SwotResult(
            SwotItem strengths,
            SwotItem weaknesses,
            SwotItem opportunities,
            SwotItem threats
    ) {}

    /**
     * Python의 SWOTItem 매핑
     */
    public record SwotItem(
            String type, // "S", "W", "O", "T"
            String keyword,
            String description,
            String diagnosis
    ) {}
}
