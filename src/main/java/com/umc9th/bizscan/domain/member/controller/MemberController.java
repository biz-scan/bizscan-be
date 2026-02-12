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
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Member", description = "회원 관련 API (회원가입, 프로필 조회, 정보 수정 및 탈퇴)")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/members")
public class MemberController {
  private final MemberCommandService memberCommandService;
  private final MemberQueryService memberQueryService;

  @Operation(
      summary = "회원가입",
      description = "신규 사용자를 회원으로 등록합니다. " + "이메일 중복 여부를 검증하고, 비밀번호는 암호화되어 저장됩니다.")
  @ApiErrorCodeExamples({
    ErrorCode.MEMBER_ALREADY_REGISTERED,
    ErrorCode.EMAIL_ALREADY_EXISTS,
    ErrorCode.NICKNAME_ALREADY_EXISTS
  })
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<Long>> register(@RequestBody @Valid RegisterMemberDto dto) {
    Long memberId = memberCommandService.registerMember(dto);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_REGISTER_SUCCESS, memberId));
  }

  // 현재 서비스에서는 사용하지 않는 API라 판단하여 주석처리
  //  @Operation(summary = "회원 단건 조회", description = "회원 ID를 통해 특정 회원의 정보를 조회합니다.")
  //  @ApiErrorCodeExamples({ErrorCode.MEMBER_NOT_FOUND})
  //  @GetMapping("/{memberId}")
  //  public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@PathVariable Long memberId) {
  //    Member member = memberQueryService.getMemberById(memberId);
  //    return ResponseEntity.ok(
  //        ApiResponse.onSuccess(SuccessCode.MEMBER_GET_SUCCESS, MemberResponseDto.from(member)));
  //  }

  //  @GetMapping
  //  @Operation(summary = "회원 목록 조회", description = "전체 회원 목록을 조회합니다.")
  //  public ResponseEntity<ApiResponse<List<MemberResponseDto>>> getMembers() {
  //
  //    List<MemberResponseDto> result =
  //        memberQueryService.getAllMembers().stream().map(MemberResponseDto::from).toList();
  //
  //    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_LIST_SUCCESS, result));
  //  }

  @PatchMapping("/{memberId}")
  @Operation(
      summary = "회원 정보 수정",
      description = "회원 닉네임 및 비밀번호를 수정합니다. 본인의 정보만 수정 가능합니다. 본인이 아닐 경우 AUTH403_1 에러가 발생합니다.")
  @ApiErrorCodeExamples(
      value = {
        ErrorCode.MEMBER_NOT_FOUND,
        ErrorCode.INVALID_PASSWORD,
        ErrorCode.SAME_AS_OLD_PASSWORD,
        ErrorCode.FORBIDDEN
      },
      security = {SecurityErrorStatus.AUTH_MUST_AUTHORIZED_URI})
  public ResponseEntity<ApiResponse<Void>> updateMember(
      @Parameter(hidden = true) @AuthenticationPrincipal User user,
      @PathVariable Long memberId,
      @RequestBody @Valid MemberUpdateRequestDto dto) {
    if (user == null) {
      throw new GeneralException(SecurityErrorStatus.AUTH_MUST_AUTHORIZED_URI);
    }

    String email = user.getUsername();
    memberCommandService.updateMember(memberId, dto, email);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_UPDATE_SUCCESS, null));
  }

  @DeleteMapping("/{memberId}")
  @Operation(summary = "회원 삭제", description = "회원을 삭제합니다.")
  @ApiErrorCodeExamples({ErrorCode.MEMBER_NOT_FOUND})
  public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long memberId) {
    memberCommandService.deleteMember(memberId);

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessCode.MEMBER_DELETE_SUCCESS, null));
  }

  @Operation(summary = "내 정보 조회", description = "JWT 인증을 통해 로그인한 사용자의 정보를 조회합니다.")
  @ApiErrorCodeExamples({ErrorCode.MEMBER_NOT_FOUND})
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<MemberResponseDto>> getMyInfo(Authentication authentication) {
    // JWT subject == email
    String email = authentication.getName();

    Member member = memberQueryService.getMemberByEmail(email);

    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessCode.MEMBER_GET_SUCCESS, MemberResponseDto.from(member)));
  }
}
