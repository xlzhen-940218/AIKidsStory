package com.xlzhen.aikidsstory.models;

public class StoryModel {
    private long id;
    private String title;
    private String content;
    private String time;
    private String audioPath;
    private boolean playing;

    public StoryModel(String title, String content, String time, String audioPath) {
        this.id = System.currentTimeMillis();
        this.title = title;
        this.content = content;
        this.time = time;
        this.audioPath = audioPath;
    }

    public StoryModel() {
        this.id = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAudioPath() {
        return audioPath;
    }

    public void setAudioPath(String audioPath) {
        this.audioPath = audioPath;
    }
}
