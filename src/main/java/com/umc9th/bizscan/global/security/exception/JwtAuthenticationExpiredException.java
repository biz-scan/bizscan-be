package com.umc9th.bizscan.global.security.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationExpiredException extends AuthenticationException {

  public static final AuthenticationException EXPIRED =
      new JwtAuthenticationExpiredException(SecurityErrorStatus.AUTH_TOKEN_HAS_EXPIRED);

  public JwtAuthenticationExpiredException(SecurityErrorStatus errorStatus) {
    super(errorStatus.name());
  }
}
