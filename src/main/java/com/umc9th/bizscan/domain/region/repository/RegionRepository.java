package com.umc9th.bizscan.domain.region.repository;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionRepository extends JpaRepository<RegionMaster, Long> {
  // CSV의 상권코드로 부모 엔티티를 찾기 위해 필수
  Optional<RegionMaster> findByTrdarCd(String trdarCd);

  List<RegionMaster> findByAdstrdCd(String adstrdCd);

  List<RegionMaster> findByAdstrdNmContaining(String keyword);

  Optional<RegionMaster> findByFullAddress(String address);

  // [✨수정] "매출 데이터(SalesEstimate)가 존재하는" 상권 중 가장 가까운 곳 찾기
  @Query(
      value =
          "SELECT r.*, "
              + "(6371 * acos(cos(radians(:lat)) * cos(radians(r.lat)) * cos(radians(r.lon) - radians(:lon)) + sin(radians(:lat)) * sin(radians(r.lat)))) AS distance "
              + "FROM tb_region_master r "
              + "JOIN tb_sales_estimate s ON r.trdar_cd = s.trdar_cd "
              + // ✨ 핵심: 데이터 있는 놈하고만 조인!
              "WHERE r.trdar_cd IS NOT NULL "
              + "GROUP BY r.id "
              + // (중복 제거용)
              "HAVING distance < 2.0 "
              + // 반경 2km까지 범위를 좀 넓혀줍니다 (데이터가 드문드문 있을 수 있으니)
              "ORDER BY distance ASC "
              + "LIMIT 1",
      nativeQuery = true)
  Optional<RegionMaster> findNearestRegionWithData(
      @Param("lat") double lat, @Param("lon") double lon);
}
