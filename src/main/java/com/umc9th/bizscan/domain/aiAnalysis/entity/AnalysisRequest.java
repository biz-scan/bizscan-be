package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.domain.aiAnalysis.enums.AnalysisStatus;
import com.umc9th.bizscan.global.common.BaseEntity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AnalysisRequest extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 프론트에서 들고 다닐 id (폴링용)
  @Column(nullable = false, unique = true)
  private String requestId;

  // 어떤 매장 분석인지
  @Column(nullable = false)
  private Long storeId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AnalysisStatus status;

  // 진행 중 메시지 (폴링 응답용)
  @Column(length = 255)
  private String progressMessage;

  // 완료 시간
  private LocalDateTime completedAt;

  // 전용 생성자 추가
  public AnalysisRequest(
      String requestId, Long storeId, AnalysisStatus status, String progressMessage) {
    this.requestId = requestId;
    this.storeId = storeId;
    this.status = status;
    this.progressMessage = progressMessage;
  }

  // ==== 상태 변경 메서드 ====

  public void start() {
    this.status = AnalysisStatus.PROCESSING;
    this.progressMessage = "매장 정보를 분석 중입니다.";
  }

  public void updateProgress(String message) {
    this.progressMessage = message;
  }

  public void complete() {
    this.status = AnalysisStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
    this.progressMessage = null;
  }

  public void fail(String message) {
    this.status = AnalysisStatus.FAILED;
    this.progressMessage = message;
  }
}
