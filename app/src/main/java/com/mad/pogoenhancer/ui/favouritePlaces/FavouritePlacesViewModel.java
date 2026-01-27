package com.mad.pogoenhancer.ui.favouritePlaces;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FavouritePlacesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FavouritePlacesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is GPX / locations fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}