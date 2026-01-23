package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.domain.aiAnalysis.enums.ActionCategory;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "action_plan")
public class ActionPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "swot_id", nullable = false)
  private Swot swot;

  @Column(nullable = false, length = 255)
  private String title;

  @Enumerated(EnumType.STRING)
  private ActionCategory category;

  @Column(columnDefinition = "json")
  private String tags;

  @Column(columnDefinition = "TEXT")
  private String reason;

  @OneToMany(mappedBy = "actionPlan", cascade = CascadeType.ALL)
  private List<ActionDetail> actionDetails = new ArrayList<>();

  @Column(columnDefinition = "TEXT")
  private String steps;
}
