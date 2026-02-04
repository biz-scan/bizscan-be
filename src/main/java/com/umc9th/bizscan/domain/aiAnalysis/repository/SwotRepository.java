package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SwotRepository extends JpaRepository<Swot, Long> {

  @Query("SELECT s FROM Swot s WHERE s.analysis.store.id = :storeId")
  List<Swot> findAllByStoreId(@Param("storeId") Long storeId);
}
