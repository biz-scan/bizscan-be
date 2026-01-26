package com.umc9th.bizscan.global.common;

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

  // FOR Refresh token(whiteList)
  // key-value 설정
  public void setValue(String refreshToken, String email) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();

    values.set(refreshToken, email, Duration.ofDays(7));
    values.set(email, refreshToken, Duration.ofDays(7));
  }

  public void deleteRefreshTokenByEmail(String email) {
    String refreshToken = redisTemplate.opsForValue().get(email).toString();
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
}
