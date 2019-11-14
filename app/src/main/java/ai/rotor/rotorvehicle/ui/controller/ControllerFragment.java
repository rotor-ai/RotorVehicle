package ai.rotor.rotorvehicle.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import ai.rotor.rotorvehicle.R;
import timber.log.Timber;

public class ControllerFragment extends Fragment {

    private ControllerViewModel controllerViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        controllerViewModel =
                ViewModelProviders.of(this).get(ControllerViewModel.class);
        View view = inflater.inflate(R.layout.fragment_controller, container, false);
        final TextView textView = view.findViewById(R.id.text_home);
        controllerViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        Timber.d("rhdebug - Creating ControllerFragment");

        return view;
    }
}