package com.umc9th.bizscan.domain.analysis.entity;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_analysis_summary")
public class AnalysisSummary {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "region_id")
  private RegionMaster regionMaster;

  private LocalDate stdDate;

  // [O: 기회]
  private String mainAgeGroup;
  private String mainGender;
  private Long avgDailyPop;
  private String peakTime;

  // [T: 위협]
  private Long competitorCount;
  private String competitionLevel;

  // [S: 강점]
  private Long avgMonIncome;

  // [Trend]
  private String housingType; // DTO의 mainHousingType 대응

  @Lob
  @Column(columnDefinition = "TEXT")
  private String topHashtags; // DTO의 topHashtags 대응

  // [W: 약점]
  private Integer myReviewCount; // 내 리뷰 수
  private Double myRating; // 내 별점
  private Double avgCompReviewCount; // 경쟁사 평균 리뷰 수

  @Lob
  @Column(columnDefinition = "TEXT")
  private String myReviewContents; // 내 리뷰 내용 요약

  @CreatedDate private LocalDateTime createdAt;
}
