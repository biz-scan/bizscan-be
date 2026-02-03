package com.umc9th.bizscan.domain.member.controller;

import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.dto.request.MemberUpdateRequestDto;
import com.umc9th.bizscan.domain.member.dto.response.MemberResponseDto;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.service.MemberCommandService;
import com.umc9th.bizscan.domain.member.service.MemberQueryService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
      description = "신규 사용자를 회원으로 등록합니다. " + "이메일 중복 여부를 검증하고, 비밀번호는 암호화되어 저장됩니다.")
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Long>> register(@RequestBody @Valid RegisterMemberDto dto) {
    Long memberId = memberCommandService.registerMember(dto);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_REGISTER_SUCCESS, memberId));
  }

  @Operation(summary = "회원 단건 조회", description = "회원 ID를 통해 특정 회원의 정보를 조회합니다.")
  @GetMapping("/{memberId}")
  public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@PathVariable Long memberId) {
    Member member = memberQueryService.getMemberById(memberId);
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessCode.MEMBER_GET_SUCCESS, MemberResponseDto.from(member)));
  }

  @GetMapping
  @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회합니다.")
  public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getMembers() {

    List<MemberResponseDto> result =
        memberQueryService.getAllMembers().stream().map(MemberResponseDto::from).toList();

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_LIST_SUCCESS, result));
  }

  @PatchMapping("/{memberId}")
  @Operation(summary = "회원 정보 수정", description = "회원 닉네임을 수정합니다.")
  public ResponseEntity<ApiResponse<Void>> updateMember(
      @PathVariable Long memberId, @RequestBody @Valid MemberUpdateRequestDto dto) {
    memberCommandService.updateMember(memberId, dto);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_UPDATE_SUCCESS, null));
  }

  @DeleteMapping("/{memberId}")
  @Operation(summary = "회원 삭제", description = "회원을 삭제합니다.")
  public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long memberId) {
    memberCommandService.deleteMember(memberId);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_DELETE_SUCCESS, null));
  }

  @Operation(summary = "내 정보 조회", description = "JWT 인증을 통해 로그인한 사용자의 정보를 조회합니다." +
          "+ storeId 응답 데이터 추가  " +
          "++ 비밀번호 변경기능은 수요일까지 만들게요...")
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MemberResponseDto>> getMyInfo(Authentication authentication) {
    // JWT subject == email
    String email = authentication.getName();

    Member member = memberQueryService.getMemberByEmail(email);

    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessCode.MEMBER_GET_SUCCESS, MemberResponseDto.from(member)));
  }
}
