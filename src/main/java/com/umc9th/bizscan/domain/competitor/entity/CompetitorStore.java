package com.umc9th.bizscan.domain.competitor.entity;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(
    name = "tb_competitor_store",
    indexes = {
      @Index(name = "idx_comp_loc", columnList = "lat, lon"), // 위치 기반 검색 최적화
      @Index(name = "idx_comp_cat_sm", columnList = "category_sm") // 업종별 필터링 최적화
    })
public class CompetitorStore {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 상가업소번호
  @Column(name = "store_uid", length = 50, unique = true)
  private String storeUid;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id", nullable = true)
  private RegionMaster regionMaster;

  @Column(name = "store_nm", length = 100)
  private String storeNm; // 상호명

  @Column(name = "branch_nm", length = 100)
  private String branchNm; // 지점명

  @Column(name = "adstrd_cd", length = 20)
  private String adstrdCd;

  @Column(name = "category_lg", length = 50)
  private String categoryLg; // 업종대분류명

  @Column(name = "category_md", length = 50)
  private String categoryMd; // 업종중분류명

  @Column(name = "category_sm", length = 50)
  private String categorySm; // 업종소분류명

  @Column(length = 200)
  private String address; // 도로명주소

  private Double lat; // 위도
  private Double lon; // 경도
}
