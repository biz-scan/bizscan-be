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

  @Query(
      value =
          "SELECT * FROM tb_sales_estimate "
              + "WHERE trdar_cd = :trdarCd "
              + "ORDER BY std_quarter DESC LIMIT 1",
      nativeQuery = true)
  Optional<SalesEstimate> findLatestByTrdarCd(@Param("trdarCd") String trdarCd);
}
