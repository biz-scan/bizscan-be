package com.umc9th.bizscan.domain.member.controller;

import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.service.MemberCommandService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberController {
  private final MemberCommandService memberCommandService;

  @GetMapping("/test")
  @ApiErrorCodeExamples({ErrorCode.BAD_REQUEST, ErrorCode.FORBIDDEN, ErrorCode.NOT_FOUND})
  public String test() {
    return "Hello, World!";
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Long>> register(@RequestBody @Valid RegisterMemberDto dto) {
    Long memberId = memberCommandService.registerMember(dto);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_REGISTER_SUCCESS, memberId));
  }
}
