package com.squad.squad.enums;

public enum TeamColor {
    WHITE,
    BLACK;

    public static TeamColor fromString(String teamColor) {
        try {
            return TeamColor.valueOf(teamColor.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid team color: " + teamColor);
        }
    }
}