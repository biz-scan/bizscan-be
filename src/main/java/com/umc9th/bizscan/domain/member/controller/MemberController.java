package com.umc9th.bizscan.domain.member.controller;


import com.umc9th.bizscan.domain.member.dto.RegisterMemberDto;
import com.umc9th.bizscan.domain.member.service.MemberCommandService;
import com.umc9th.bizscan.domain.member.service.MemberCommandServiceImpl;
import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/member")
public class MemberController {
    private final MemberCommandServiceImpl memberCommandService;

    @GetMapping("/test")
    @ApiErrorCodeExamples({ErrorCode.BAD_REQUEST, ErrorCode.FORBIDDEN, ErrorCode.NOT_FOUND})
    public String test() {
        return "Hello, World!";
    }

    @PostMapping("/register")
    public ResponseEntity<Long> register(
            @RequestBody @Valid RegisterMemberDto registerMemberDto
    ) {
        return ResponseEntity.ok(
                memberCommandService.registerMember(registerMemberDto)
        );
    }
}
