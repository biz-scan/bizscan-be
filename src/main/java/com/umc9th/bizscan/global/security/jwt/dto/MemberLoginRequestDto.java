package com.umc9th.bizscan.global.security.jwt.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberLoginRequestDto {

  private String email;
  private String password;
}
