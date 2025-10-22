package com.xlzhen.aikidsstory;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;
import com.xlzhen.aikidsstory.config.SettingsConfig;
import com.xlzhen.aikidsstory.utils.StorageUtils;

public class SettingsActivity extends AppCompatActivity {
    private SettingsConfig settingsConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        settingsConfig = StorageUtils.getData(this, "config", SettingsConfig.class);
        if (settingsConfig == null) {
            settingsConfig = new SettingsConfig();
        }
        RadioGroup radioGroup = findViewById(R.id.radio_group_view);
        radioGroup.check(settingsConfig.isMan() ? R.id.father_radio_button : R.id.monther_radio_button);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            settingsConfig.setMan(checkedId == R.id.father_radio_button);
            StorageUtils.saveData(this, "config", settingsConfig);
        });

        Slider slider = findViewById(R.id.speed_slider);
        float speed = 10f * (settingsConfig.getSpeed() - 1f) + 5f;
        slider.setValue(speed);
        slider.addOnChangeListener((slider1, value, fromUser) -> {
            settingsConfig.setSpeed(1f + (value - 5) * 0.1f);
            StorageUtils.saveData(this, "config", settingsConfig);
        });
    }
}
