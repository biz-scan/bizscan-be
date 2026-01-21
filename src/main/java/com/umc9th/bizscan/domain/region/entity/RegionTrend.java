package com.umc9th.bizscan.domain.region.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tb_region_trend")
public class RegionTrend {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionMaster regionMaster;

    private String keyword;      // 연관 키워드 (예: 성수동 카페)

    private Long searchVol;      // 검색량 (건수니까 Long이 적절)

    @Column(name = "search_rank") // rank는 DB 예약어일 가능성이 있어서 이름 변경
    private Long rank;           // 순위 (1위, 2위...)

    private LocalDate stdDate;   // 수집 기준일 (오늘 날짜)
}