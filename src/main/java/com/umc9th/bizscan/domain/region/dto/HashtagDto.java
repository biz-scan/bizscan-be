package com.umc9th.bizscan.domain.region.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HashtagDto {
    private String hashtag;   // 예: #성수동크로플
    private Long searchVolume; // 검색량 (높은 순 정렬용)
}