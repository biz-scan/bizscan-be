package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
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

  // 프론트에서 들고 다닐 ID (폴링용)
  @Column(nullable = false, unique = true)
  private String requestId;

  // 어떤 분석인지
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "analysis_id")
  private Analysis analysis;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private AnalysisStatus status;

  // 진행 중 메시지 (폴링 응답용)
  @Column(length = 255)
  private String progressMessage;

  // 완료 시간
  private LocalDateTime completedAt;

  // ===== 상태 전이 메서드 =====

  public void updateProgress(String message) {
    this.progressMessage = message;
  }

  public void complete(String catchphrase) {
    this.status = AnalysisStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
    this.progressMessage = null;
  }

  public void fail(String message) {
    this.status = AnalysisStatus.FAILED;
    this.progressMessage = message;
  }

  public void updateStatus(AnalysisStatus status) {
    this.status = status;
  }
}
