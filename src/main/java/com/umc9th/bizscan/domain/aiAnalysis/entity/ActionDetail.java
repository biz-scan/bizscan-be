package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class ActionDetail extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "action_plan_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private ActionPlan actionPlan;

  private String title;

  private Integer step;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(columnDefinition = "TEXT")
  private String expectedOutcome;

  private Boolean isCompleted;

  public void updateIsCompleted(Boolean isCompleted) {
    this.isCompleted = isCompleted;
  }
}
