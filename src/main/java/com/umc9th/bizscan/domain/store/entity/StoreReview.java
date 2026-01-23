package com.umc9th.bizscan.domain.store.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tb_store_review")
public class StoreReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String placeId; // 네이버 플레이스 ID (어떤 가게인지 식별)

  @Column(columnDefinition = "TEXT")
  private String content; // 리뷰 내용

  @Builder
  public StoreReview(String placeId, String content) {
    this.placeId = placeId;
    this.content = content;
  }
}
