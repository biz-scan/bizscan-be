package com.umc9th.bizscan.domain.region.repository;

import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegionRepository extends JpaRepository<RegionMaster, Long> {
  Optional<RegionMaster> findByTrdarCd(String trdarCd);

  List<RegionMaster> findByAdstrdCd(String adstrdCd);

  Optional<RegionMaster> findByFullAddress(String address);

  @Query(
      value =
          "SELECT r.*, "
              + "(6371 * acos(cos(radians(:lat)) * cos(radians(r.lat)) * cos(radians(r.lon) - radians(:lon)) + sin(radians(:lat)) * sin(radians(r.lat)))) AS distance "
              + "FROM tb_region_master r "
              + "JOIN tb_sales_estimate s ON r.trdar_cd = s.trdar_cd "
              + "WHERE r.trdar_cd IS NOT NULL "
              + "GROUP BY r.id "
              + "HAVING distance < 2.0 "
              + "ORDER BY distance ASC "
              + "LIMIT 1",
      nativeQuery = true)
  Optional<RegionMaster> findNearestRegionWithData(
      @Param("lat") double lat, @Param("lon") double lon);
}
