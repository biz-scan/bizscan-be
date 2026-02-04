package com.umc9th.bizscan.domain.actionnote.entity;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class ActionNote extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_id", nullable = false)
  private ActionPlan actionPlan;

  @Column(nullable = false)
  private Boolean isCompleted;

  // 완료 상태 변경
  public void updateIsCompleted(Boolean isCompleted) {
    this.isCompleted = isCompleted;
  }
}
