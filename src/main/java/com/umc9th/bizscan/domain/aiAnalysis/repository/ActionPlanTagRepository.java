package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlanTag;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionPlanTagRepository extends JpaRepository<ActionPlanTag, Long> {
}
