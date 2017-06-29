package com.example.trantrungduong95.truesms.Presenter;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.example.trantrungduong95.truesms.R;

import java.util.List;

public final class SettingsNewActivity extends PreferenceActivity
        implements IPreferenceContainer {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings);
        Utils.setLocale(this);
    }

    @Override
    public void onBuildHeaders(final List<Header> target) {
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
    protected boolean isValidFragment(final String fragmentName) {
        return HeaderPreferenceFragment.class.getName().equals(fragmentName);
    }
}
