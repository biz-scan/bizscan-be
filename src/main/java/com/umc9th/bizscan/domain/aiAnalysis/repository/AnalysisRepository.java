package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import com.umc9th.bizscan.domain.store.entity.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

  Optional<Analysis> findByStoreId(@Param("storeId") Long storeId);

  Optional<Analysis> findByStore(Store store);

  @Query(
      """
          select a.id
          from Analysis a
          where a.store.id = :storeId
          order by a.createdAt desc
      """)
  Optional<Long> findLatestAnalysisIdByStoreId(@Param("storeId") Long storeId);

  @Modifying(clearAutomatically = true) // 쿼리 실행 후 영속성 컨텍스트를 자동으로 비워줌
  @Query("delete from Analysis a where a.id = :analysisId")
  void deleteByIdBulk(@Param("analysisId") Long analysisId);
}
