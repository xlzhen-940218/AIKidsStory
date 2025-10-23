package com.xlzhen.aikidsstory;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.slider.Slider;
import com.xlzhen.aikidsstory.config.SettingsConfig;
import com.xlzhen.aikidsstory.databinding.ActivitySettingsBinding;
import com.xlzhen.aikidsstory.viewmodel.HistoryActivityViewModel;
import com.xlzhen.aikidsstory.viewmodel.SettingsActivityViewModel;
import com.xlzhen.mvvm.activity.BaseActivity;
import com.xlzhen.mvvm.storage.StorageUtils;

public class SettingsActivity extends BaseActivity<ActivitySettingsBinding, SettingsActivityViewModel> {
    private SettingsConfig settingsConfig;

    @Override
    protected int getVariableId() {
        return BR.settings;
    }

    @Override
    protected SettingsActivityViewModel bindingModel() {
        return new SettingsActivityViewModel(this);
    }

    @Override
    protected void initData() {
        settingsConfig = StorageUtils.getData(this, "config", SettingsConfig.class);
        if (settingsConfig == null) {
            settingsConfig = new SettingsConfig();
        }

        binding.radioGroupView.check(settingsConfig.isMan() ? R.id.father_radio_button : R.id.monther_radio_button);
        binding.radioGroupView.setOnCheckedChangeListener((group, checkedId) -> {

            settingsConfig.setMan(checkedId == R.id.father_radio_button);
            StorageUtils.saveData(this, "config", settingsConfig);
        });

        float speed = 10f * (settingsConfig.getSpeed() - 1f) + 5f;
        binding.speedSlider.setValue(speed);
        binding.speedSlider.addOnChangeListener((slider1, value, fromUser) -> {
            settingsConfig.setSpeed(1f + (value - 5) * 0.1f);
            StorageUtils.saveData(this, "config", settingsConfig);
        });
    }

    @Override
    protected ActivitySettingsBinding bindingInflate() {
        return ActivitySettingsBinding.inflate(getLayoutInflater());
    }

   
}
