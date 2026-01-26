package com.umc9th.bizscan.global.security.controller;


import com.umc9th.bizscan.domain.member.service.MemberQueryService;
import com.umc9th.bizscan.global.apiPayload.ApiResponse;
import com.umc9th.bizscan.global.apiPayload.code.SuccessCode;
import com.umc9th.bizscan.global.security.jwt.dto.JwtToken;
import com.umc9th.bizscan.global.security.jwt.dto.MemberLoginRequestDto;
import com.umc9th.bizscan.global.security.jwt.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "Token API", description = "JWT 토큰 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tokens")
public class TokenApiController {

    private final TokenService tokenService;
    private final MemberQueryService memberQueryService;

    @Operation(summary = "이메일로 JWT 토큰 발급")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtToken>> login(
            @RequestBody MemberLoginRequestDto memberLoginRequestDto
    ) {
        JwtToken token = tokenService.login(memberLoginRequestDto);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SuccessCode.MEMBER_LOGIN_SUCCESS,
                        token
                )
        );
    }

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access Token을 재발급합니다.")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<JwtToken>> issueToken(
            @RequestParam String refresh
    ) {
        JwtToken token = tokenService.issueTokens(refresh);

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SuccessCode.TOKEN_REISSUE_SUCCESS,
                        token
                )
        );
    }

    @Operation(
            summary = "이메일 중복 검증",
            description = "이미 존재하면 true, 사용 가능하면 false를 반환합니다."
    )
    @GetMapping("/duplication/login-id")
    public ResponseEntity<ApiResponse<Boolean>> idValidator(
            @RequestParam String email
    ) {
        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SuccessCode.MEMBER_DUPLICATION_CHECK_SUCCESS,
                        memberQueryService.validateExistEmail(email)
                )
        );
    }

    @Operation(
            summary = "닉네임 중복 검증",
            description = "이미 존재하면 true, 사용 가능하면 false를 반환합니다."
    )
    @GetMapping("/duplication/nickname")
    public ResponseEntity<ApiResponse<Boolean>> nicknameValidator(
            @RequestParam String nickname
    ) {
        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        SuccessCode.MEMBER_DUPLICATION_CHECK_SUCCESS,
                        memberQueryService.validateExistNickname(nickname)
                )
        );
    }
}