package com.squad.squad.service;

import java.time.Duration;
import java.time.LocalDateTime;

/** Maç saatleri: backend'de maç süresi/bitiş anı tutulmuyor, başlangıç saatinden sayılıyor. */
public final class MatchTiming {

    /** Canlı pencere: başlangıçtan itibaren gol girişinin "maç esnası" sayıldığı süre (60 dk maç + 30 dk). */
    public static final Duration LIVE_WINDOW = Duration.ofMinutes(90);

    private MatchTiming() {
    }

    /** Maç (ve son gol girişleri) bitti mi: bundan sonraki gol girişi/düzeltmesi bildirim üretir. */
    public static boolean isAfterLiveWindow(LocalDateTime kickoff, LocalDateTime now) {
        return kickoff != null && now.isAfter(kickoff.plus(LIVE_WINDOW));
    }
}
