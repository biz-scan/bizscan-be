package com.umc9th.bizscan.domain.aiAnalysis.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FailCallbackReqDTO(
    boolean isSuccess,
    String code,
    String message,
    @JsonProperty("request_id") String requestId,
    String status)
    implements CommonCallback {}
