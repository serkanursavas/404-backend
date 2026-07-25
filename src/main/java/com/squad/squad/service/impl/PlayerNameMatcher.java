package com.squad.squad.service.impl;

import com.squad.squad.entity.Player;
import com.squad.squad.entity.Roster;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Apple Watch dikteyle gelen oyuncu ismini, aktif maçın roster'ındaki oyunculara eşleştirir.
 * Türkçe karakter normalizasyonu + tam/kısmi eşleşme + Levenshtein benzerliği kullanır.
 * Tek bir güvenli eşleşme yoksa (0 ya da birden fazla aday) boş döner — çağıran taraf
 * bu durumda golü eklemeyip kullanıcıya "oyuncu bulunamadı" der.
 */
@Component
public class PlayerNameMatcher {

    public Optional<Roster> match(String dictatedName, List<Roster> rosters) {
        if (dictatedName == null || dictatedName.isBlank() || rosters == null || rosters.isEmpty()) {
            return Optional.empty();
        }

        String query = normalize(dictatedName);

        List<Roster> exact = rosters.stream()
                .filter(r -> normalize(fullName(r.getPlayer())).equals(query))
                .toList();
        if (exact.size() == 1) {
            return Optional.of(exact.get(0));
        }

        List<Roster> contains = rosters.stream()
                .filter(r -> {
                    Player player = r.getPlayer();
                    String full = normalize(fullName(player));
                    String name = normalize(player.getName());
                    String surname = normalize(player.getSurname());
                    return full.contains(query)
                            || (!name.isEmpty() && query.contains(name))
                            || (!surname.isEmpty() && query.contains(surname));
                })
                .toList();
        if (contains.size() == 1) {
            return Optional.of(contains.get(0));
        }

        return closestFuzzyMatch(query, rosters);
    }

    private Optional<Roster> closestFuzzyMatch(String query, List<Roster> rosters) {
        Roster best = null;
        int bestDistance = Integer.MAX_VALUE;
        int secondBestDistance = Integer.MAX_VALUE;

        for (Roster roster : rosters) {
            int distance = StringUtils.getLevenshteinDistance(query, normalize(fullName(roster.getPlayer())));
            if (distance < bestDistance) {
                secondBestDistance = bestDistance;
                bestDistance = distance;
                best = roster;
            } else if (distance < secondBestDistance) {
                secondBestDistance = distance;
            }
        }

        int threshold = Math.max(2, query.length() / 3);
        boolean unambiguous = bestDistance < secondBestDistance;
        if (best != null && bestDistance <= threshold && unambiguous) {
            return Optional.of(best);
        }
        return Optional.empty();
    }

    private String fullName(Player player) {
        String name = player.getName() == null ? "" : player.getName();
        String surname = player.getSurname() == null ? "" : player.getSurname();
        return (name + " " + surname).trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String lower = value.toLowerCase(Locale.forLanguageTag("tr"))
                .replace('ı', 'i').replace('İ', 'i')
                .replace('ş', 's').replace('ç', 'c')
                .replace('ğ', 'g').replace('ö', 'o').replace('ü', 'u');
        return lower.replaceAll("[^a-z ]", "").trim().replaceAll("\\s+", " ");
    }
}
