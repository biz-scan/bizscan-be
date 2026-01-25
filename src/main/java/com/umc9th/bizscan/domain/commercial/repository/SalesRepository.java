package com.umc9th.bizscan.domain.commercial.repository;

import com.umc9th.bizscan.domain.commercial.entity.SalesEstimate;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesRepository extends JpaRepository<SalesEstimate, Long> {

  Optional<SalesEstimate> findTopByRegionMasterOrderByStdQuarterDesc(RegionMaster regionMaster);

  Optional<SalesEstimate> findTopByTrdarCdOrderByStdQuarterDesc(String trdarCd);

  // 가장 최근 분기(std_quarter 내림차순) 데이터 1개를 확실하게 가져옴
  @Query(
      value =
          "SELECT * FROM tb_sales_estimate "
              + "WHERE trdar_cd = :trdarCd "
              + "ORDER BY std_quarter DESC LIMIT 1",
      nativeQuery = true)
  Optional<SalesEstimate> findLatestByTrdarCd(@Param("trdarCd") String trdarCd);
}
