package com.squad.squad.dto;

import java.util.Map;

public class ExpoPushMessage {

    private String to;
    private String title;
    private String body;
    private Map<String, Object> data;
    private String sound = "default";

    public ExpoPushMessage(String to, String title, String body, Map<String, Object> data) {
        this.to = to;
        this.title = title;
        this.body = body;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }
}
