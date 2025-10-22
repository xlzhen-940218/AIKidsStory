package com.xlzhen.aikidsstory;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.xlzhen.aikidsstory.adapter.HistoryAdapter;
import com.xlzhen.aikidsstory.config.SettingsConfig;
import com.xlzhen.aikidsstory.models.StoryModelList;
import com.xlzhen.aikidsstory.utils.StorageUtils;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {
    private HistoryAdapter historyAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.history), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        SettingsConfig settingsConfig = StorageUtils.getData(this, "config", SettingsConfig.class);
        if(settingsConfig == null){
            settingsConfig = new SettingsConfig();
        }
        ListView historyListView = findViewById(R.id.history_list_view);
        StoryModelList list =  StorageUtils.getData(this, "history", StoryModelList.class);
        if(list == null){
            list = new StoryModelList(new ArrayList<>());
        }
        historyAdapter = new HistoryAdapter(this, list.getStoryModels(), R.layout.adapter_history, settingsConfig.getSpeed());
        historyListView.setAdapter(historyAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(historyAdapter != null){
            historyAdapter.stopPlay();
        }
    }
}
