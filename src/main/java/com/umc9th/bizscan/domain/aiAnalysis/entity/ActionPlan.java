package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.domain.actionnote.entity.ActionNote;
import com.umc9th.bizscan.domain.aiAnalysis.enums.RelatedSwotType;
import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class ActionPlan extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "analysis_id", nullable = false)
  private Analysis analysis;

  // AI가 부여한 임시 ID (매칭용)
  private Integer aiRefId;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String reason;

  @Enumerated(EnumType.STRING)
  private RelatedSwotType relatedSwot;

  @OneToMany(mappedBy = "actionPlan", cascade = CascadeType.ALL)
  private List<ActionDetail> details = new ArrayList<>();

  @OneToMany(mappedBy = "actionPlan", cascade = CascadeType.ALL)
  private List<ActionPlanTag> tags = new ArrayList<>();

  @OneToOne(mappedBy = "actionPlan", cascade = CascadeType.ALL)
  private ActionNote actionNote;

  public void removeActionNote() {
    this.actionNote = null;
  }
}
