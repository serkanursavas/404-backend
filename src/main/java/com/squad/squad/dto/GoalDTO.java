package com.squad.squad.dto;

public class GoalDTO {
    private Integer game_id;
    private Integer player_id;
    private String player_name;
    private String team_color;

    public GoalDTO(Integer game_id, Integer player_id, String player_name, String team_color) {
        this.game_id = game_id;
        this.player_id = player_id;
        this.player_name = player_name;
        this.team_color = team_color;
    }

    public Integer getGame_id() {
        return game_id;
    }

    public void setGame_id(Integer game_id) {
        this.game_id = game_id;
    }

    public Integer getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(Integer player_id) {
        this.player_id = player_id;
    }

    public String getPlayer_name() {
        return player_name;
    }

    public void setPlayer_name(String player_name) {
        this.player_name = player_name;
    }

    public String getTeam_color() {
        return team_color;
    }

    public void setTeam_color(String team_color) {
        this.team_color = team_color;
    }

}
