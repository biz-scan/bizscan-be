package com.umc9th.bizscan.global.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
public class HealthController {
    @Operation(
            summary = "서버 상태 확인",
            description = "서버가 정상적으로 기동 중인지 확인하기 위한 API입니다. 'Hello, World!'를 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "서버 상태 정상",
                            content = @Content(schema = @Schema(implementation = String.class, example = "Hello, World!"))
                    )
            }
    )
  @GetMapping("/health")
  public String health() {
    return "Hello, World!";
  }
}
