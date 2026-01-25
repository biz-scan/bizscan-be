package com.umc9th.bizscan.domain.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginRequestDto {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
