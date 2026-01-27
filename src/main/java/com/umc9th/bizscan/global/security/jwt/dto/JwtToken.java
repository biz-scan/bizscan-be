package com.umc9th.bizscan.global.security.jwt.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtToken {
  private String grantType; // JWT에 대한 인증 타입. Bearer 사용. 이후 HTTP 헤더에 prefix로 붙여줌
  private String accessToken;
  private String refreshToken;
  private Date accessTokenExpire;
  private Date refreshTokenExpire;
  private String role;
}
