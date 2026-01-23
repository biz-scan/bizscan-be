package com.umc9th.bizscan.domain.hinterland.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tb_housing_stat")
public class HousingStat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String zoneNm; // 권역명
  private String stdDate;

  // 비율 (BigDecimal 필수)
  private Double singleFamRatio;
  private Double multiFamRatio;
  private Double multiplexHouseRatio;
  private Double townHouseRatio;
  private Double aptRatio;
  private Double officetelRatio;
  private Double studioRatio;
}
