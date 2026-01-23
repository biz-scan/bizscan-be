package com.umc9th.bizscan.domain.aiAnalysis.entity;

import com.umc9th.bizscan.global.common.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "swot")
public class Swot extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 매장 ID
  @Column(name = "store_id", nullable = false)
  private Long storeId;

  // 뱃지
  private String badge;

  // Strength
  @Column(columnDefinition = "TEXT")
  private String sTitle;

  @Column(columnDefinition = "TEXT")
  private String sDetail;

  // Weakness
  @Column(columnDefinition = "TEXT")
  private String wTitle;

  @Column(columnDefinition = "TEXT")
  private String wDetail;

  // Opportunity
  @Column(columnDefinition = "TEXT")
  private String oTitle;

  @Column(columnDefinition = "TEXT")
  private String oDetail;

  // Threat
  @Column(columnDefinition = "TEXT")
  private String tTitle;

  @Column(columnDefinition = "TEXT")
  private String tDetail;

  // ActionPlan 연결
  @OneToMany(mappedBy = "swot", cascade = CascadeType.ALL)
  private List<ActionPlan> actionPlans = new ArrayList<>();

  // Service에서 사용하는 전용 생성자
  public Swot(
      Long storeId,
      String sTitle,
      String sDetail,
      String wTitle,
      String wDetail,
      String oTitle,
      String oDetail,
      String tTitle,
      String tDetail) {
    this.storeId = storeId;
    this.sTitle = sTitle;
    this.sDetail = sDetail;
    this.wTitle = wTitle;
    this.wDetail = wDetail;
    this.oTitle = oTitle;
    this.oDetail = oDetail;
    this.tTitle = tTitle;
    this.tDetail = tDetail;
  }
}
