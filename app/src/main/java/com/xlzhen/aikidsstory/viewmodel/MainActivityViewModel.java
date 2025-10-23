package com.xlzhen.aikidsstory.viewmodel;

import android.view.View;

import androidx.lifecycle.MutableLiveData;

import com.xlzhen.aikidsstory.MainActivity;
import com.xlzhen.mvvm.binding.base.BaseActivityViewModel;

public class MainActivityViewModel extends BaseActivityViewModel<MainActivity> {
    public final MutableLiveData<Boolean> isStoryGenerating = new MutableLiveData<>(false);
    public final MutableLiveData<String> audioPath = new MutableLiveData<>("");
    public MainActivityViewModel(MainActivity activity) {
        super(activity);
    }

    @Override
    public void onResume() {

    }

    @Override
    public void backPage(View view) {

    }
}
