package com.umc9th.bizscan.domain.commercial.entity;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tb_sales_estimate")
public class SalesEstimate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private RegionMaster regionMaster;

  @Column(name = "trdar_cd", length = 20)
  private String trdarCd; // 상권_코드

  @Column(name = "trdar_cd_nm", length = 100)
  private String trdarCdNm; // 상권_코드_명

  @Column(name = "service_cd", length = 20)
  private String serviceCd; // 서비스_업종_코드

  @Column(name = "service_nm", length = 100)
  private String serviceNm; // 서비스_업종_코드_명

  @Column(name = "std_quarter", length = 10)
  private String stdQuarter; // 기준_년분기_코드

  // --- 매출 건수 데이터 ---

  @Column(name = "total_sale_cnt")
  private Long totalSaleCnt; // 당월_매출_건수

  @Column(name = "male_count")
  private Long maleCount; // 남성_매출_건수

  @Column(name = "female_count")
  private Long femaleCount; // 여성_매출_건수

  // --- 연령대별 데이터 ---

  @Column(name = "age_10_count")
  private Long age10Count;

  @Column(name = "age_20_count")
  private Long age20Count;

  @Column(name = "age_30_count")
  private Long age30Count;

  @Column(name = "age_40_count")
  private Long age40Count;

  @Column(name = "age_50_count")
  private Long age50Count;

  @Column(name = "age_60_count")
  private Long age60Count;

  // 시간대별 매출 건수
  @Column(name = "tmzon_00_06_sale_cnt")
  private Long time0006; // 새벽

  @Column(name = "tmzon_06_11_sale_cnt")
  private Long time0611; // 오전

  @Column(name = "tmzon_11_14_sale_cnt")
  private Long time1114; // 점심

  @Column(name = "tmzon_14_17_sale_cnt")
  private Long time1417; // 오후

  @Column(name = "tmzon_17_21_sale_cnt")
  private Long time1721; // 저녁

  @Column(name = "tmzon_21_24_sale_cnt")
  private Long time2124; // 심야
}
