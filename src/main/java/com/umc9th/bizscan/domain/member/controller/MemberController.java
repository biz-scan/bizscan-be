package com.umc9th.bizscan.domain.member.controller;


import com.umc9th.bizscan.global.apiPayload.code.ErrorCode;
import com.umc9th.bizscan.global.config.swagger.ApiErrorCodeExamples;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    @GetMapping("/test")
    @ApiErrorCodeExamples({ErrorCode.BAD_REQUEST, ErrorCode.FORBIDDEN, ErrorCode.NOT_FOUND})
    public String test() {
        return "Hello, World!";
    }
}
