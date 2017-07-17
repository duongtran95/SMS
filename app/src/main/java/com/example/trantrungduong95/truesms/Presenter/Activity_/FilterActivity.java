package com.example.trantrungduong95.truesms.Presenter.Activity_;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.PagerFilterAdapter;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Presenter.SettingsNewActivity;
import com.example.trantrungduong95.truesms.Presenter.SettingsOldActivity;
import com.example.trantrungduong95.truesms.R;

public class FilterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
/*        setTheme(SettingsOldActivity.getTheme(this));
        Utils.setLocale(this);*/
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blacklist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initInstancesDrawer();

    }

    //Tabview
    private void initInstancesDrawer() {
        TabLayout tabs = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager pager = (ViewPager) findViewById(R.id.vpPager);
        PagerFilterAdapter adapter = new PagerFilterAdapter(getSupportFragmentManager(), this);

        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                Color.GREEN,
                Color.RED,
                Color.BLACK,
                Color.BLUE
        };

        //pager.setBackgroundColor(Color.DKGRAY);
        ColorStateList myList = new ColorStateList(states, colors);
        tabs.setTabTextColors(myList);

    }
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        return super.onPrepareOptionsPanel(view, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in Action Bar clicked; go home
                Intent intent = new Intent(this, MainActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                startActivity(intent);
                return true;
            case R.id.action_search:
                Toast.makeText(this, "search", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.item_settings_blacklist_filterd:
                if (Build.VERSION.SDK_INT >= 19) {
                    startActivity(new Intent(this, SettingsNewActivity.class));
                } else {
                    startActivity(new Intent(this, SettingsOldActivity.class));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.blacklist_activity, menu);
        return true;
    }
}
