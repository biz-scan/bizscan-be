package com.umc9th.bizscan.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai.fastapi")
public class FastApiProperties {

  private String baseUrl;
  private String analysisPath;
  private String diagnosisPath;
}
