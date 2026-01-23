package com.umc9th.bizscan.domain.aiAnalysis.controller;

import com.umc9th.bizscan.domain.aiAnalysis.service.ActionPlanService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/action-plans")
public class ActionPlanController {

    private final ActionPlanService actionPlanService;

    @PostMapping("/{storeId}")
    public ApiResponse<?> generateActionPlan(@PathVariable Long storeId) {
        actionPlanService.generateActionPlan(storeId);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
