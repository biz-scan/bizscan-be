package com.umc9th.bizscan.global.security.config;

import com.umc9th.bizscan.global.security.exception.JwtAccessDeniedHandler;
import com.umc9th.bizscan.global.security.exception.JwtAuthenticationEntryPoint;
import com.umc9th.bizscan.global.security.filter.JwtAuthenticationFilter;
import com.umc9th.bizscan.global.security.filter.JwtExceptionFilter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
  private final String[] allowUris = {
    "/api/tokens/login",
    "/api/members/register",
    "/health",
    "/api/analysis/callback/**",
    "/api/swot/summary",
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
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth -> auth.requestMatchers(allowUris).permitAll().anyRequest().authenticated())
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

  // Security 레벨에서의 CORS 세부 설정
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(
        Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://bizscan.duckdns.org",
            "https://bizscan-web.vercel.app"));
    configuration.setAllowedMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
    configuration.setAllowedHeaders(
        Arrays.asList("Authorization", "Content-Type", "x-requested-with"));
    configuration.setAllowCredentials(true);
    configuration.setExposedHeaders(Arrays.asList("Authorization"));
    configuration.setMaxAge(3600L); // 브라우저가 CORS 응답을 캐싱할 시간 (초)

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
