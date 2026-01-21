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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionMaster regionMaster;

    private String stdQuarter;     // 기준분기
    private Long totalSaleCnt;     // 총 매출 건수

    private Long age10Count;
    private Long age20Count;
    private Long age30Count;
    private Long age40Count;
    private Long age50Count;
    private Long age60Count;

    private Long maleCount;
    private Long femaleCount;
}