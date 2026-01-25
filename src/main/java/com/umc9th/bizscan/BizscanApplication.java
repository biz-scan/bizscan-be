package com.umc9th.bizscan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BizscanApplication {

  public static void main(String[] args) {
    SpringApplication.run(BizscanApplication.class, args);
  }
}
