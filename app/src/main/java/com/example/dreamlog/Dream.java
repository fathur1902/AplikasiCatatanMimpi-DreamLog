package com.example.dreamlog;

public class Dream {
    private Long id;
    private String title;
    private String description;
    private String emotion;

    public Dream(String title, String description, String emotion) {
        this.title = title;
        this.description = description;
        this.emotion = emotion;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getEmotion() { return emotion; }
}