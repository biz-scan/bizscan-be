package com.umc9th.bizscan.domain.actionnote.repository;

import com.umc9th.bizscan.domain.actionnote.entity.ActionNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActionNoteRepository extends JpaRepository<ActionNote, Long> {
    Optional<ActionNote> findByActionPlanId(Long actionPlanId);
    boolean existsByActionPlanId(Long actionPlanId);
}
