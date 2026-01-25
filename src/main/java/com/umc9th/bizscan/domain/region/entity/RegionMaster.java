package com.umc9th.bizscan.domain.region.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tb_region_master")
public class RegionMaster {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = false)
  private String trdarCd;

  @Column(length = 10)
  private String guCd;

  private String adstrdCd;

  // 전체 주소 검색
  private String fullAddress;

  private String trdarCdNm;
  private String trdarSeCd;
  private String trdarSeCdNm;
  private String guNm;
  private String adstrdNm;

  // 좌표
  private Double xCoord;
  private Double yCoord;
  private Double lat;
  private Double lon;
  private Double areaSize;
}
