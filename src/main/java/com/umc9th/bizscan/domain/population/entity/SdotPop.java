package com.umc9th.bizscan.domain.population.entity;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "tb_sdot_pop")
public class SdotPop {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private RegionMaster regionMaster;

  private String modelNm;
  private String serialNo;
  private String placeNm;
  private String adstrdNm;

  private LocalDateTime measureDate;
  private Long popCount;
}
