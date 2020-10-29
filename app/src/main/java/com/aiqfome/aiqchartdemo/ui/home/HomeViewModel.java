package com.aiqfome.aiqchartdemo.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final MutableLiveData<String> dText = new MutableLiveData<>();

    public HomeViewModel() {
        mText.setValue("Welcome to aiqChart!");
        dText.setValue("Click on menu and enjoy our charts examples ;)");
    }

    public LiveData<String> getHomeText() {
        return mText;
    }
    public LiveData<String> getDescriptionText() {
        return dText;
    }
}