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

  // 핵심 코드

  @Column(nullable = false, unique = true)
  private String trdarCd; // 1. 상권코드 (자식 데이터 & 매출/인구 특성 연결용)

  @Column(length = 10)
  private String guCd; // 2. 자치구코드 (New! S-DoT 유동인구 연결용, 예: 11200)

  private String adstrdCd; // 3. 행정동코드 (배후지 소득/주거 & 경쟁업체 연결용)

  // 정보

  private String trdarCdNm; // 상권명 (예: 성수동 갈비골목)
  private String trdarSeCd; // 상권구분코드
  private String trdarSeCdNm; // 상권구분명
  private String guNm; // 자치구명 (예: 성동구 -> 권역 매핑에도 사용)
  private String adstrdNm; // 행정동명 (예: 성수2가1동)

  // 좌표

  private Integer xCoord;
  private Integer yCoord;

  private Double lat; // 위도 (Naver Map 마커용)

  private Double lon; // 경도

  private Double areaSize; // 면적
}
