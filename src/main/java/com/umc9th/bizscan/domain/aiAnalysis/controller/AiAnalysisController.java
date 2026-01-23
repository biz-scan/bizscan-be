package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.service.AiAnalysisService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ai-analysis")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    @PostMapping
    public ApiResponse<Void> analyze(@RequestParam Long storeId) {
        aiAnalysisService.analyzeStore(storeId);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }

    @GetMapping("/{storeId}")
    public ApiResponse<?> getLatestSwot(@PathVariable Long storeId) {
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                aiAnalysisService.getLatestSwot(storeId)
        );
    }
}
