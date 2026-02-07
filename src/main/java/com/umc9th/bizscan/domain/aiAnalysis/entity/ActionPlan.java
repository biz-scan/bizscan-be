package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.domain.actionnote.entity.ActionNote;
import com.umc9th.bizscan.domain.aiAnalysis.enums.RelatedSwotType;
import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Analysis analysis;

  // AI가 부여한 임시 ID (매칭용)
  private Integer aiRefId;

  private String title;

  @Column(columnDefinition = "TEXT")
  private String reason;

  @Enumerated(EnumType.STRING)
  private RelatedSwotType relatedSwot;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "actionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ActionDetail> details = new ArrayList<>();

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "actionPlan", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ActionPlanTag> tags = new ArrayList<>();

  @OneToOne(mappedBy = "actionPlan", cascade = CascadeType.ALL)
  private ActionNote actionNote;

  public void removeActionNote() {
    this.actionNote = null;
  }
}
