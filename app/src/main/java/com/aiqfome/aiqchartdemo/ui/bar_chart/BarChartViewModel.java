package com.aiqfome.aiqchartdemo.ui.bar_chart;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BarChartViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BarChartViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is barchart fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}