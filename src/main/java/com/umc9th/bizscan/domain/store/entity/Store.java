package com.umc9th.bizscan.domain.store.entity;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.member.entity.Member;
import com.umc9th.bizscan.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@SuperBuilder
@Table(name = "store")
public class Store extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY) // ManyToOne에서 OneToOne으로 변경
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, length = 255)
  private String address;

  @Column(precision = 10, scale = 7)
  private BigDecimal lat;

  @Column(precision = 10, scale = 7)
  private BigDecimal lon;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private StoreCategory category;

  @Enumerated(EnumType.STRING)
  @Column(name = "category_detail", nullable = false, length = 50)
  private StoreCategoryDetail categoryDetail;

  @Column(length = 255)
  private String signature;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PriceRange price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private Target target;

  @Enumerated(EnumType.STRING)
  @Column(name = "pain_point", nullable = false, length = 30)
  private PainPoint painPoint;

  @OneToOne(mappedBy = "store", cascade = CascadeType.ALL)
  private Analysis analysis;

  public void update(
      String name,
      String address,
      BigDecimal lat,
      BigDecimal lon,
      StoreCategory category,
      StoreCategoryDetail categoryDetail,
      String signature,
      PriceRange price,
      Target target,
      PainPoint painPoint) {

    this.name = name;
    this.address = address;
    this.lat = lat;
    this.lon = lon;
    this.category = category;
    this.categoryDetail = categoryDetail;
    this.signature = signature;
    this.price = price;
    this.target = target;
    this.painPoint = painPoint;
  }
}
