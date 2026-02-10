package com.umc9th.bizscan.global.security.config;

import com.umc9th.bizscan.global.security.exception.JwtAccessDeniedHandler;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationEntryPoint;
import com.umc9th.bizscan.global.security.filter.JwtAuthenticationFilter;
import com.umc9th.bizscan.global.security.filter.JwtExceptionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  private final String[] allowUris = {
    "/api/v1/tokens/login",
    "/api/v1/member/register",
    "/swagger-ui/**",
    "/swagger-resources/**",
    "/v3/api-docs/**",
  };

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtExceptionFilter jwtExceptionFilter,
      JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
      JwtAccessDeniedHandler jwtAccessDeniedHandler)
      throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth
                    // .requestMatchers(allowUris).permitAll().anyRequest().authenticated()
                    .anyRequest()
                    .permitAll())
        .formLogin(AbstractHttpConfigurer::disable)
        .exceptionHandling(
            handler ->
                handler
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint) // 401 에러 핸들링
                    .accessDeniedHandler(jwtAccessDeniedHandler) // 403 에러 핸들링
            );

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(jwtExceptionFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
