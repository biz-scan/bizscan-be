package com.umc9th.bizscan.domain.member.controller;

import com.umc9th.bizscan.domain.member.dto.MemberResponseDto;
import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.service.MemberCommandService;
import com.umc9th.bizscan.domain.member.service.MemberQueryService;
import com.umc9th.bizscan.domain.store.dto.response.StoreResponse;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberController {
  private final MemberCommandService memberCommandService;
  private final MemberQueryService memberQueryService;

  @GetMapping("/test")
  @ApiErrorCodeExamples({ErrorCode.BAD_REQUEST, ErrorCode.FORBIDDEN, ErrorCode.NOT_FOUND})
  public String test() {
    return "Hello, World!";
  }

  @Operation(
          summary = "회원가입",
          description = "신규 사용자를 회원으로 등록합니다. " +
                  "이메일 중복 여부를 검증하고, 비밀번호는 암호화되어 저장됩니다."
  )
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Long>> register(@RequestBody @Valid RegisterMemberDto dto) {
    Long memberId = memberCommandService.registerMember(dto);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_REGISTER_SUCCESS, memberId));
  }

  @Operation(
          summary = "회원 단건 조회",
          description = "회원 ID를 통해 특정 회원의 정보를 조회합니다."
  )
  @GetMapping("/{memberId}")
  public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@PathVariable Long memberId) {
    Member member = memberQueryService.getMemberById(memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_GET_SUCCESS, MemberResponseDto.from(member)
            )
    );
  }

}