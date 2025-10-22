package com.xlzhen.aikidsstory.models;

public class RequestStoryModel {
    private String user_request;
    private String language;

    public RequestStoryModel(String user_request, String language) {
        this.user_request = user_request;
        this.language = language;
    }

    public RequestStoryModel() {
    }

    public String getUser_request() {
        return user_request;
    }

    public void setUser_request(String user_request) {
        this.user_request = user_request;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
