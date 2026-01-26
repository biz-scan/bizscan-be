package com.umc9th.bizscan.global.security.exception;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {
  public static final AuthenticationException UNAUTHORIZED_LOGIN_DATA =
      new JwtAuthenticationException(
          SecurityErrorStatus.AUTH_UNAUTHORIZED_LOGIN_DATA_RETRIEVAL_ERROR);
  public static final AuthenticationException ASSIGNABLE_PARAMETER =
      new JwtAuthenticationException(SecurityErrorStatus.AUTH_ASSIGNABLE_PARAMETER);

  public static final AuthenticationException WRONG_PASSWORD =
      new JwtAuthenticationException(SecurityErrorStatus.AUTH_WRONG_PASSWORD);
  public static final AuthenticationException INVALID_TOKEN =
      new JwtAuthenticationException(SecurityErrorStatus.AUTH_INVALID_TOKEN);

  public static final AuthenticationException TOKEN_IS_UNSUPPORTED =
      new JwtAuthenticationException(SecurityErrorStatus.AUTH_TOKEN_IS_UNSUPPORTED);

  public static final AuthenticationException AUTH_NULL =
      new JwtAuthenticationException(SecurityErrorStatus.AUTH_IS_NULL);
  public static final AuthenticationException TOKEN_IS_EXPIRED =
      new JwtAuthenticationException(SecurityErrorStatus.AUTH_TOKEN_HAS_EXPIRED);

  public JwtAuthenticationException(SecurityErrorStatus errorStatus) {
    super(errorStatus.name());
  }
}
