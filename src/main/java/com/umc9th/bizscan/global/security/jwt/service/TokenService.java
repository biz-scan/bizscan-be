package com.umc9th.bizscan.global.security.jwt.service;

import com.umc9th.bizscan.global.security.jwt.dto.JwtToken;
import com.umc9th.bizscan.global.security.jwt.dto.MemberLoginRequestDto;
import java.util.Date;
import org.springframework.security.core.Authentication;

public interface TokenService {

  JwtToken login(MemberLoginRequestDto memberLoginRequestDto);

  JwtToken issueTokens(String refreshToken);

  JwtToken generateToken(Authentication authentication);

  Authentication getAuthentication(String accessToken);

  boolean validateToken(String token);

  boolean logout(String refreshToken);

  boolean existsRefreshToken(String refreshToken);

  Date parseExpiration(String token);
}
