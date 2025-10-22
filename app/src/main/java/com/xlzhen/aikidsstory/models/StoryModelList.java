package com.xlzhen.aikidsstory.models;

import java.util.List;

public class StoryModelList {
    private List<StoryModel> storyModels;

    public StoryModelList(List<StoryModel> storyModels) {
        this.storyModels = storyModels;
    }

    public StoryModelList() {
    }

    public List<StoryModel> getStoryModels() {
        return storyModels;
    }

    public void setStoryModels(List<StoryModel> storyModels) {
        this.storyModels = storyModels;
    }
}
