package com.squad.squad.dto.notification;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class NotificationResponse {

    private Integer id;
    private String title;
    private String body;
    private String type;
    private String data;

    @JsonProperty("isRead")
    private Boolean isRead;

    private LocalDateTime createdAt;

    public NotificationResponse() {}

    public NotificationResponse(Integer id, String title, String body, String type, String data, Boolean isRead, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.type = type;
        this.data = data;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
