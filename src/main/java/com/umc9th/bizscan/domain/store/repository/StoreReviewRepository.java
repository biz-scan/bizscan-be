package com.umc9th.bizscan.domain.store.repository;

import com.umc9th.bizscan.domain.store.entity.StoreReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreReviewRepository extends JpaRepository<StoreReview, Long> {
  // placeId로 저장된 리뷰가 있는지 찾는 메소드
  List<StoreReview> findByPlaceId(String placeId);

  // 이미 수집한 가게인지 확인하는 메소드 (존재 여부)
  boolean existsByPlaceId(String placeId);
}
