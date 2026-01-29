package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.AnalysisRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, Long> {
  Optional<AnalysisRequest> findByRequestId(String requestId);

  Optional<AnalysisRequest> findTopByStoreIdOrderByCreatedAtDesc(Long storeId);
}
