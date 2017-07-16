package com.example.trantrungduong95.truesms.CustomAdapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.example.trantrungduong95.truesms.Presenter.Fragment_.Fragment_Blacklist;
import com.example.trantrungduong95.truesms.Presenter.Fragment_.Fragment_Conv_Blacklist;
import com.example.trantrungduong95.truesms.R;

import java.util.HashMap;


/**
 * Created by ngomi_000 on 6/1/2017.
 */

public class PagerBlacklistAdapter extends FragmentStatePagerAdapter {
    private HashMap<Integer, Fragment> fragmentHashMap;
    private int NUM_ITEMS = 2;
    Context context;

    public PagerBlacklistAdapter(FragmentManager fragmentManager,Context nContext) {
        super(fragmentManager);
        context = nContext;
        fragmentHashMap = new HashMap<>();
        fragmentHashMap.put(0,new Fragment_Conv_Blacklist());
        fragmentHashMap.put(1,new Fragment_Blacklist());

    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        return fragmentHashMap.get(position);
        /*switch (position) {
            case 0:// Fragment # 0 - This will show FirstFragment different title
                return new Fragment_Conv_Blacklist();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return new Fragment_Blacklist();
            default:
                return null;
        }*/
    }

    // Returns the page title for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.title_Conv_Backlist);
            case 1:
                return context.getString(R.string.title_Backlist);
        }
        return null;
    }



}
