package com.umc9th.bizscan.domain.aiAnalysis.enums;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public enum RelatedSwotType {
  SO,
  ST,
  WO,
  WT,
  UNDEFINED;

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
