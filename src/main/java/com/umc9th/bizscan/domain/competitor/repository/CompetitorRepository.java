package com.umc9th.bizscan.domain.competitor.repository;

import com.umc9th.bizscan.domain.competitor.entity.CompetitorStore;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompetitorRepository extends JpaRepository<CompetitorStore, Long> {
  List<CompetitorStore> findByRegionMaster(RegionMaster regionMaster);

  // 행정동 코드로 찾기
  List<CompetitorStore> findByAdstrdCd(String adstrdCd);

  @Query(
      value =
          "SELECT COUNT(*) FROM tb_competitor_store s "
              + "WHERE s.category_sm LIKE CONCAT('%', :category, '%') "
              + "AND (6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lon) - radians(:lon)) + sin(radians(:lat)) * sin(radians(s.lat)))) <= :radiusKm",
      nativeQuery = true)
  int countCompetitors(
      @Param("lat") double lat,
      @Param("lon") double lon,
      @Param("category") String category,
      @Param("radiusKm") double radiusKm);

  // 2. 주변 경쟁 업체 리스트 조회 (W분석용)
  // 카운트만 하는게 아니라, 실제 가게 정보를 가져옵니다. (최대 5개 제한)
  @Query(
      value =
          "SELECT * FROM tb_competitor_store s "
              + "WHERE s.category_sm LIKE CONCAT('%', :keyword, '%') "
              + "AND (6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lon) - radians(:lon)) + sin(radians(:lat)) * sin(radians(s.lat)))) <= :radiusKm "
              + "LIMIT 5",
      nativeQuery = true)
  List<CompetitorStore> findNearbyCompetitors(
      @Param("lat") double lat,
      @Param("lon") double lon,
      @Param("keyword") String keyword,
      @Param("radiusKm") double radiusKm);
}
