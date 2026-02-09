package com.umc9th.bizscan.global.security.config;

import com.umc9th.bizscan.global.security.filter.JwtAuthenticationFilter;
import com.umc9th.bizscan.global.security.filter.JwtExceptionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtExceptionFilter jwtExceptionFilter)
      throws Exception {
    http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth -> auth.anyRequest().permitAll() // TODO : 추후 권한 설정
            )
        .formLogin(form -> form.disable());

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
