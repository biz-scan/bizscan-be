package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {

  //  List<ActionPlan> findAllBySwot_Id(Long swotId);

  //  List<ActionPlan> findAllBySwot_StoreId(Long storeId);

  Optional<ActionPlan> findByAnalysisAndAiRefId(Analysis analysis, Integer aiRefId);
}
