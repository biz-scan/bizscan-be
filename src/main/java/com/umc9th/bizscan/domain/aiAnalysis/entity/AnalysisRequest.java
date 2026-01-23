package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class AnalysisRequest extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long storeId;

  @Enumerated(EnumType.STRING)
  private AnalysisStatus status;

  public AnalysisRequest toDone() {
    this.status = AnalysisStatus.DONE;
    return this;
  }
}
