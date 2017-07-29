package com.example.trantrungduong95.truesms.Presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.example.trantrungduong95.truesms.Presenter.Activity_.SettingsOldActivity;

public class HeaderPreferenceFragment extends PreferenceFragment implements IPreferenceContainer {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Activity a = getActivity();
        int res = a.getResources().getIdentifier(getArguments().getString("resource"), "xml", a.getPackageName());
        addPreferencesFromResource(res);

        SettingsOldActivity.registerOnPreferenceClickListener(this);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }
}
