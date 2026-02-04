package com.umc9th.bizscan.domain.actionnote.converter;

import com.umc9th.bizscan.domain.actionnote.entity.ActionNote;
import com.umc9th.bizscan.domain.aiAnalysis.entity.ActionPlan;

public class ActionNoteConverter {
  public static ActionNote toAddEntity(ActionPlan actionPlan) {
    return ActionNote.builder().actionPlan(actionPlan).isCompleted(false).build();
  }
}
