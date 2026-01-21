package com.umc9th.bizscan.domain.competitor.entity;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tb_competitor_store")
public class CompetitorStore {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionMaster regionMaster;

    private String storeNm;
    private String branchNm;
    private String adstrdCd;

    private String categoryLg;
    private String categoryMd;
    private String categorySm;
    private String address;

    @Column(precision = 10, scale = 7)
    private Double lat;
    @Column(precision = 10, scale = 7)
    private Double lon;
}
