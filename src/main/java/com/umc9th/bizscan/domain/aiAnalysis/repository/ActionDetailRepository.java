package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionDetail;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActionDetailRepository extends JpaRepository<ActionDetail, Long> {

  // ActionNote 관련 쿼리
  // ActionPlan 리스트에 속하는 모든 ActionDetail을 한 번에 조회
  @Query("SELECT ad FROM ActionDetail ad WHERE ad.actionPlan IN :actionPlans ORDER BY ad.step ASC")
  List<ActionDetail> findAllByActionPlanIn(@Param("actionPlans") List<ActionPlan> actionPlans);

  @Query(
      "SELECT ad FROM ActionDetail ad "
          + "JOIN FETCH ad.actionPlan ap "
          + "LEFT JOIN FETCH ap.actionNote "
          + "JOIN FETCH ap.details "
          + // 진행도 계산을 위해 모든 상세 미션들을 함께 로드
          "WHERE ad.id = :id")
  Optional<ActionDetail> findByIdWithPlanAndNoteAndDetails(@Param("id") Long id);
}
