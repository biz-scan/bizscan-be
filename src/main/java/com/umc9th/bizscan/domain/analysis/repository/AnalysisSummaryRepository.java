package com.umc9th.bizscan.domain.analysis.repository;

import com.umc9th.bizscan.domain.analysis.entity.AnalysisSummary;
import com.umc9th.bizscan.domain.region.entity.RegionMaster;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisSummaryRepository extends JpaRepository<AnalysisSummary, Long> {

  List<AnalysisSummary> findByRegionMasterOrderByCreatedAtDesc(RegionMaster regionMaster);
}
