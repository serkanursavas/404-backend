package com.squad.squad.service;

/**
 * Apple Shortcuts (Watch dikte) üzerinden canlı gol girişi için servis.
 * Tüm metodlar seslendirilecek düz metin döner.
 */
public interface ShortcutGoalService {

    String addGoalByPlayerName(String dictatedName);

    String undoLastGoal();

    String getScoreText();

    String finishMatch();
}
