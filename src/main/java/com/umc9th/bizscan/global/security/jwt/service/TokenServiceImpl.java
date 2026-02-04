package com.umc9th.bizscan.global.security.jwt.service;

import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.domain.member.service.MemberQueryService;
import com.umc9th.bizscan.global.apiPayload.exception.GeneralException;
import com.umc9th.bizscan.global.security.consts.StaticVariable;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationException;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationExpiredException;
import com.umc9th.bizscan.global.security.exception.SecurityErrorStatus;
import com.umc9th.bizscan.global.security.jwt.dto.JwtToken;
import com.umc9th.bizscan.global.security.jwt.dto.MemberLoginRequestDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TokenServiceImpl implements TokenService {
  private final Key key; // security yml 파일 생성 후 app.jwt.secret에 값 넣어주기(보안을 위해 따로 연락주세요)
  private final RedisService redisService;
  private final MemberQueryService memberQueryService;
  private final PasswordEncoder passwordEncoder;

  public TokenServiceImpl(
      @Value("${app.jwt.secret}") String key,
      RedisService redisService,
      PasswordEncoder passwordEncoder,
      MemberQueryService memberQueryService) {
    byte[] keyBytes = Decoders.BASE64.decode(key);
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.redisService = redisService;
    this.memberQueryService = memberQueryService;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public JwtToken login(MemberLoginRequestDto memberLoginRequestDto) {
    Member member = memberQueryService.getMemberByEmail(memberLoginRequestDto.getEmail());
    if (!passwordEncoder.matches(memberLoginRequestDto.getPassword(), member.getPassword())) {
      // TODO security exception 아래와 같이 변경하기
      throw JwtAuthenticationException.WRONG_PASSWORD;
    }
    Authentication authentication =
        new UsernamePasswordAuthenticationToken(member, "", member.getAuthorities());
    return generateToken(authentication);
  }

  @Override
  public JwtToken issueTokens(String refreshToken) {
    refreshToken = resolveToken(refreshToken);

    if (!validateToken(refreshToken) || !existsRefreshToken(refreshToken)) {
      throw new GeneralException(SecurityErrorStatus.AUTH_INVALID_REFRESH_TOKEN);
    }

    redisService.deleteValue(refreshToken);

    Claims claims = parseClaims(refreshToken);
    String email = claims.getSubject();
    Member member = memberQueryService.getMemberByEmail(email);

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(member, "", member.getAuthorities());

    return generateToken(authentication);
  }

  @Override
  public JwtToken generateToken(Authentication authentication) {
    // 권한 가져오기
    String authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    long now = (new Date()).getTime();
    Date accessTokenExpiresIn = new Date(now + StaticVariable.ACCESS_TOKEN_EXPIRE_TIME);

    log.info("date = {}", accessTokenExpiresIn);
    String accessToken =
        Jwts.builder()
            .setSubject(authentication.getName())
            .claim("auth", authorities)
            .setExpiration(accessTokenExpiresIn)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

    // Refresh Token 생성
    String refreshToken =
        Jwts.builder()
            .setSubject(authentication.getName())
            .setExpiration(new Date(now + StaticVariable.REFRESH_TOKEN_EXPIRE_TIME)) // 7일
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

    // 새 리프레시 토큰을 Redis에 저장
    redisService.setValue(refreshToken, authentication.getName());

    return JwtToken.builder()
        .grantType("Bearer")
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpire(parseExpiration(accessToken))
        .refreshTokenExpire(parseExpiration(refreshToken))
        .role(authentication.getAuthorities().toString())
        .build();
  }

  @Override
  public Authentication getAuthentication(String accessToken) {
    // Jwt 토큰 복호화
    Claims claims = parseClaims(accessToken);

    if (claims.get("auth") == null) {
      throw new JwtAuthenticationException(SecurityErrorStatus.AUTH_INVALID_TOKEN);
    }

    // 클레임에서 권한 정보 가져오기
    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("auth").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    // UserDetails 객체를 만들어서 Authentication return
    // UserDetails: interface, User: UserDetails를 구현한 class
    UserDetails principal = new User(claims.getSubject(), "", authorities);
    return new UsernamePasswordAuthenticationToken(principal, "", authorities);
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
      return true;
    } catch (SecurityException | MalformedJwtException e) {
      log.info("Invalid JWT Token", e);
      throw JwtAuthenticationException.INVALID_TOKEN;
    } catch (ExpiredJwtException e) {
      log.info("Expired JWT Token", e);
      throw JwtAuthenticationExpiredException.EXPIRED;
    } catch (UnsupportedJwtException e) {
      log.info("Unsupported JWT Token", e);
      throw JwtAuthenticationException.TOKEN_IS_UNSUPPORTED;
    } catch (IllegalArgumentException e) {
      log.info("JWT claims string is empty.", e);
      throw JwtAuthenticationException.AUTH_NULL;
    }
  }

  @Override
  public boolean logout(String email) {
    // email 기준으로 Redis에 저장된 refreshToken 찾아서 삭제
    redisService.deleteRefreshTokenByEmail(email);
    return true;
  }

  @Override
  public boolean existsRefreshToken(String refreshToken) {
    return redisService.getValue(refreshToken) != null;
  }

  @Override
  public Date parseExpiration(String token) {
    try {
      Claims claims =
          Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

      return claims.getExpiration();
    } catch (JwtException exception) {
      throw new JwtAuthenticationException(SecurityErrorStatus.AUTH_INVALID_TOKEN);
    }
  }

  private Claims parseClaims(String accessToken) {
    try {
      return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  private String resolveToken(String token) {
    if (token != null && token.startsWith("Bearer ")) {
      return token.substring(7);
    }
    return token;
  }
}
