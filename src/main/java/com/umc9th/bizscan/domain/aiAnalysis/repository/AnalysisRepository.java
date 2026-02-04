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
  WHERE a.store.id = :storeId
""")
  Optional<Analysis> findByStoreIdWithSwot(@Param("storeId") Long storeId);

  @Query(
      """
    SELECT DISTINCT a FROM Analysis a
    LEFT JOIN FETCH a.actionPlans ap
    LEFT JOIN FETCH ap.tags
    WHERE a.store.id = :storeId
""")
  Optional<Analysis> findByStoreIdWithActionPlan(@Param("storeId") Long storeId);
}
