package com.umc9th.bizscan.domain.analysis.repository;

import com.umc9th.bizscan.domain.analysis.entity.AnalysisSummary;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisSummaryRepository extends JpaRepository<AnalysisSummary, Long> {

  // 특정 상권(Region)의 최신 분석 내역들을 조회할 때 사용해
  List<AnalysisSummary> findByRegionMasterOrderByCreatedAtDesc(RegionMaster regionMaster);
}
