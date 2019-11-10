package ai.rotor.rotorvehicle.ui.controller;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ControllerViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ControllerViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the controller fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}