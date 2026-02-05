package com.umc9th.bizscan.domain.store.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "StoreTagUpdateRequest")
public class StoreTagUpdateRequest {

  @NotNull(message = "tags는 필수입니다.")
  @Size(min = 1, max = 3, message = "tags는 1개 이상 3개 이하로 보내야 합니다.")
  @Schema(description = "태그 코드 (1~3개)", example = "[\"MOOD_VIEW\",\"FEATURE_GOOD_VALUE\"]")
  private List<String> tags;
}
