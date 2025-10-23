package com.xlzhen.aikidsstory.viewmodel;

import android.view.View;

import com.xlzhen.aikidsstory.HistoryActivity;
import com.xlzhen.aikidsstory.R;
import com.xlzhen.aikidsstory.adapter.HistoryAdapter;
import com.xlzhen.aikidsstory.config.SettingsConfig;
import com.xlzhen.aikidsstory.models.StoryModelList;
import com.xlzhen.mvvm.binding.base.BaseActivityViewModel;
import com.xlzhen.mvvm.storage.StorageUtils;

import java.util.ArrayList;

public class HistoryActivityViewModel extends BaseActivityViewModel<HistoryActivity> {
    public HistoryAdapter historyAdapter;
    public HistoryActivityViewModel(HistoryActivity activity) {
        super(activity);
        SettingsConfig settingsConfig = StorageUtils.getData(activity, "config", SettingsConfig.class);
        if(settingsConfig == null){
            settingsConfig = new SettingsConfig();
        }
        StoryModelList list =  StorageUtils.getData(activity, "history", StoryModelList.class);
        if(list == null){
            list = new StoryModelList(new ArrayList<>());
        }
        historyAdapter = new HistoryAdapter(activity, list.getStoryModels(), R.layout.adapter_history, settingsConfig.getSpeed());
    }

    @Override
    public void onResume() {

    }

    @Override
    public void backPage(View view) {

    }
}
