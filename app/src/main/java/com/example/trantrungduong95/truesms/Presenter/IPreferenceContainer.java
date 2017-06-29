package com.example.trantrungduong95.truesms.Presenter;
import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
/**
 * Created by ngomi_000 on 6/15/2017.
 */

public interface IPreferenceContainer {

    Preference findPreference(CharSequence key);

    Context getContext();

    Activity getActivity();
}
