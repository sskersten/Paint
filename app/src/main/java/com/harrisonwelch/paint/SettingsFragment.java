package com.harrisonwelch.paint;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,  Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_settings, container, false);
        //todo: Setup onclicklisteners for settings buttons

        return result;
    }
}
