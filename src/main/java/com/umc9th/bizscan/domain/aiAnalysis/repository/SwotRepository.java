package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SwotRepository extends JpaRepository<Swot, Long> {

  Optional<Swot> findTopByStoreIdOrderByCreatedAtDesc(Long storeId);
}
