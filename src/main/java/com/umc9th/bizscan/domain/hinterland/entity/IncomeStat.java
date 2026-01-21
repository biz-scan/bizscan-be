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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String adstrdCd; // 행정동 코드
    private String adstrdNm;
    private String stdDate;

    private Long avgMonIncome;
    private Long incomeSecCd;
}