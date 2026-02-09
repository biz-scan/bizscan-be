package com.umc9th.bizscan.domain.store.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tb_store_crawling_data")
public class StoreCrawlingData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String placeId;
  private String storeName;

  private int reviewCount; // 리뷰 수
  private double rating;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public StoreCrawlingData(
      String placeId,
      String storeName,
      int reviewCount,
      double rating,
      double avgCompReviewCount,
      String reviewContents) {
    this.placeId = placeId;
    this.storeName = storeName;
    this.reviewCount = reviewCount;
    this.rating = rating;
  }

  public void updateData(int reviewCount, double rating) {
    this.reviewCount = reviewCount;
    this.rating = rating;
  }
}
