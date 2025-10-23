package com.xlzhen.aikidsstory;

import com.xlzhen.aikidsstory.databinding.ActivityHistoryBinding;
import com.xlzhen.aikidsstory.viewmodel.HistoryActivityViewModel;
import com.xlzhen.mvvm.activity.BaseActivity;

public class HistoryActivity extends BaseActivity <ActivityHistoryBinding, HistoryActivityViewModel>{
    @Override
    protected int getVariableId() {
        return BR.history;
    }

    @Override
    protected HistoryActivityViewModel bindingModel() {
        return new HistoryActivityViewModel(this);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected ActivityHistoryBinding bindingInflate() {
        return ActivityHistoryBinding.inflate(getLayoutInflater());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(model.historyAdapter != null){
            model.historyAdapter.stopPlay();
        }
    }

    @Override
    protected boolean isNotificationBarTextBlack() {
        return false;
    }

}
