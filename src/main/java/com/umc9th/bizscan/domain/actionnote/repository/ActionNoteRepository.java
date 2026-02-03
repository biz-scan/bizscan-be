package com.umc9th.bizscan.domain.actionnote.repository;

import com.umc9th.bizscan.domain.actionnote.entity.ActionNote;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionNoteRepository extends JpaRepository<ActionNote, Long> {
  Optional<ActionNote> findByActionPlanId(Long actionPlanId);

  boolean existsByActionPlanId(Long actionPlanId);
}
