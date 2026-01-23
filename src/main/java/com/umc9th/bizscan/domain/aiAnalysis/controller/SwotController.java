package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.dto.request.SwotAnalyzeRequest;
import com.umc9th.bizscan.domain.aiAnalysis.dto.response.SwotResponse;
import com.umc9th.bizscan.domain.aiAnalysis.service.SwotService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai/swot")
public class SwotController {

    private final SwotService swotService;

    @GetMapping("/{storeId}")
    public ApiResponse<SwotResponse> getSwot(@PathVariable Long storeId) {

        SwotResponse response = swotService.getLatestSwot(storeId);

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }

    @PostMapping
    public ApiResponse<SwotResponse> analyzeSwot(
            @RequestBody SwotAnalyzeRequest request
    ) {
        SwotResponse response = swotService.generateSwot(request.getStoreId());
        
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                response
        );
    }
}
