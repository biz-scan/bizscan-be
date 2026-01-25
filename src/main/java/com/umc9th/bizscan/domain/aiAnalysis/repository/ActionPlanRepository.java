package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {

  List<ActionPlan> findAllBySwot_Id(Long swotId);

  List<ActionPlan> findAllBySwot_StoreId(Long storeId);
}
