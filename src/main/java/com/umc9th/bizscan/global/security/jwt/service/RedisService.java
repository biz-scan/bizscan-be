package com.umc9th.bizscan.global.security.jwt.service;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

  private final RedisTemplate redisTemplate;
  private static final String BLACKLIST_PREFIX = "blacklist:";

  // FOR Refresh token(whiteList)
  // key-value 설정
  public void setValue(String refreshToken, String email) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();

    values.set(refreshToken, email, Duration.ofDays(7));
    values.set(email, refreshToken, Duration.ofDays(7));
  }

  public void deleteRefreshTokenByEmail(String email) {
    String refreshToken = (String) redisTemplate.opsForValue().get(email);
    if (refreshToken != null) {
      redisTemplate.delete(refreshToken);
      redisTemplate.delete(email);
    }
  }

  // key 값으로 value 가져오기
  public String getValue(String token) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();
    return values.get(token);
  }

  public void deleteValue(String token) {
    if (token != null && token.startsWith("Bearer ")) {
      // "Bearer " 접두사 제거
      token = token.substring(7);
    }
    redisTemplate.delete(token);
  }

  // AccessToken 블랙리스트 등록 (TTL: 남은 만료시간)
  public void blacklistAccessToken(String accessToken, Duration ttl) {
    if (accessToken == null) {
      return;
    }
    if (accessToken.startsWith("Bearer ")) {
      accessToken = accessToken.substring(7);
    }

    if (ttl == null || ttl.isZero() || ttl.isNegative()) {
      return;
    }

    redisTemplate.opsForValue().set(BLACKLIST_PREFIX + accessToken, "logout", ttl);
  }

  // 블랙리스트 여부 확인
  public boolean isBlacklisted(String accessToken) {
    if (accessToken == null) {
      return false;
    }
    if (accessToken.startsWith("Bearer ")) {
      accessToken = accessToken.substring(7);
    }

    Boolean exists = redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken);
    return Boolean.TRUE.equals(exists);
  }
}
