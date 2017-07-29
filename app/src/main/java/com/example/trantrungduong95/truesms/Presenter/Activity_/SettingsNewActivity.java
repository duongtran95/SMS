package com.example.trantrungduong95.truesms.Presenter.Activity_;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.trantrungduong95.truesms.Presenter.HeaderPreferenceFragment;
import com.example.trantrungduong95.truesms.Presenter.IPreferenceContainer;
import com.example.trantrungduong95.truesms.Presenter.Utils;
import com.example.trantrungduong95.truesms.R;

import java.util.List;

public final class SettingsNewActivity extends PreferenceActivity implements IPreferenceContainer {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        Utils.setLocale(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return HeaderPreferenceFragment.class.getName().equals(fragmentName);
    }
}
