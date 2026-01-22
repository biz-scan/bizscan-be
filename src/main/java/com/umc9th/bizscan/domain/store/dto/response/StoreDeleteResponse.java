package com.umc9th.bizscan.domain.store.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(title = "StoreDeleteResponse: 가게 삭제 응답 DTO")
public class StoreDeleteResponse {

  @Schema(description = "삭제된 가게 ID", example = "1")
  private Long storeId;

  public static StoreDeleteResponse of(Long storeId) {
    return StoreDeleteResponse.builder().storeId(storeId).build();
  }
}