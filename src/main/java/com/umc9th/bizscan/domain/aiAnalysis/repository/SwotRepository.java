package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.Swot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SwotRepository extends JpaRepository<Swot,Long> {

    Optional<Swot> findTopByStoreIdOrderByCreatedAtDesc(Long storeId);
}
