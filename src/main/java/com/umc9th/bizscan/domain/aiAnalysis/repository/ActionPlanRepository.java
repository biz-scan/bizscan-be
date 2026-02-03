package com.umc9th.bizscan.domain.aiAnalysis.repository;

import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;
import com.umc9th.bizscan.domain.aiAnalysis.entity.Analysis;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {

  //  List<ActionPlan> findAllBySwot_Id(Long swotId);

  //  List<ActionPlan> findAllBySwot_StoreId(Long storeId);

  Optional<ActionPlan> findByAnalysisAndAiRefId(Analysis analysis, Integer aiRefId);

  // ActionNote(실행노트) 관련 쿼리
    @Query("SELECT DISTINCT ap FROM ActionPlan ap " +
            "JOIN FETCH ap.analysis a " +
            "JOIN FETCH ap.tags t " +
            "JOIN FETCH ap.actionNote an " +
            "WHERE a.store.id = :storeId " +
            "AND an.isCompleted = :isCompleted")
    List<ActionPlan> findAllByStoreIdAndCompletion(
            @Param("storeId") Long storeId,
            @Param("isCompleted") Boolean isCompleted
    );

    // Tags만 FetchJoin
    @Query("SELECT ap FROM ActionPlan ap LEFT JOIN FETCH ap.tags WHERE ap.id = :id")
    Optional<ActionPlan> findByIdWithTags(@Param("id") Long id);

    // Details만 FetchJoin
    @Query("SELECT ap FROM ActionPlan ap " +
            "JOIN FETCH ap.details ad " +
            "WHERE ap.id = :id " +
            "ORDER BY ad.step ASC")
    Optional<ActionPlan> findByIdWithDetails(@Param("id") Long id);


}
