package com.squad.squad.service;

import java.time.Duration;
import java.time.LocalDateTime;

/** Maç saatleri: backend'de maç süresi/bitiş anı tutulmuyor, başlangıç saatinden sayılıyor. */
public final class MatchTiming {

    /** Maç süresi: başlangıç + 60 dk = maç bitti. Bitişe kadar girilen goller bildirim üretmez. */
    public static final Duration MATCH_DURATION = Duration.ofMinutes(60);

    private MatchTiming() {
    }

    /** Maç bitti mi: bundan sonraki gol girişi/düzeltmesi bildirim üretir. */
    public static boolean isMatchOver(LocalDateTime kickoff, LocalDateTime now) {
        return kickoff != null && now.isAfter(kickoff.plus(MATCH_DURATION));
    }
}
