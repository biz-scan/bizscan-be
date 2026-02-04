package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
  Optional<Analysis> findByStoreId(long id);

  @Query(
      """
  SELECT DISTINCT a FROM Analysis a
  LEFT JOIN FETCH a.swots s
  LEFT JOIN FETCH a.actionPlans ap
  LEFT JOIN FETCH ap.tags t
  LEFT JOIN FETCH ap.details d
  WHERE a.store.id = :storeId
""")
  Optional<Analysis> findByStoreIdWithAll(@Param("storeId") Long storeId);
}
