package com.umc9th.bizscan.domain.aiAnalysis.enums;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum RelatedSwotType {
    SO(SwotType.S, SwotType.O),
    ST(SwotType.S, SwotType.T),
    WO(SwotType.W, SwotType.O),
    WT(SwotType.W, SwotType.T),
    UNDEFINED(null, null);

    private final SwotType first;
    private final SwotType second;

    RelatedSwotType(SwotType first, SwotType second) {
        this.first = first;
        this.second = second;
    }

    // "S"를 넣으면 [SO, ST]를 반환하는 메서드
    public static List<RelatedSwotType> findAllByComponent(SwotType type) {
        return Arrays.stream(values())
                .filter(v -> v.first == type || v.second == type)
                .collect(Collectors.toList());
    }

  public static RelatedSwotType from(List<String> swotList) {
    if (swotList == null || swotList.size() < 2) {
      return null;
    }

    // 대문자 변환 및 Set으로 변환
    Set<String> set = swotList.stream().map(String::toUpperCase).collect(Collectors.toSet());

    if (set.contains("S") && set.contains("O")) return SO;
    if (set.contains("S") && set.contains("T")) return ST;
    if (set.contains("W") && set.contains("O")) return WO;
    if (set.contains("W") && set.contains("T")) return WT;

    return UNDEFINED;
  }
}
