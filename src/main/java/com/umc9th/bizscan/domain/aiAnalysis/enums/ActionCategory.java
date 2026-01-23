package com.umc9th.bizscan.domain.aiAnalysis.enums;

public enum ActionCategory {
  MARKETING,
  MENU,
  OPERATION;

  public static ActionCategory from(String value) {
    return ActionCategory.valueOf(value.toUpperCase());
  }
}
