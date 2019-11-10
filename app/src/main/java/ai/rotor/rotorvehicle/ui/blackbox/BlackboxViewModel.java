package ai.rotor.rotorvehicle.ui.blackbox;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BlackboxViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public BlackboxViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is the blackbox fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}