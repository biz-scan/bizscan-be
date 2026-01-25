package com.umc9th.bizscan.domain.hinterland.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tb_income_stat")
public class IncomeStat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String adstrdCd; // 행정동 코드
  private String adstrdNm; // 행정동 명
  private String stdDate; // 기준 년월

  private Long avgMonIncome; // 월 평균 소득
  private String incomeDecile; // 소득구간
}
