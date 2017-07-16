package com.example.trantrungduong95.truesms.Presenter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.trantrungduong95.truesms.CustomAdapter.PagerBlacklistAdapter;
import com.example.trantrungduong95.truesms.MainActivity;
import com.example.trantrungduong95.truesms.Model.Block;
import com.example.trantrungduong95.truesms.Presenter.Fragment_.Fragment_Blacklist;
import com.example.trantrungduong95.truesms.Presenter.Fragment_.Fragment_Conv_Blacklist;
import com.example.trantrungduong95.truesms.R;

import java.util.ArrayList;
import java.util.List;

public class BlacklistActivity extends AppCompatActivity {
    public List<Block> blockList = new ArrayList<Block>();
    public SpamHandler db;
    private PagerBlacklistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
/*        setTheme(SettingsOldActivity.getTheme(this));
        Utils.setLocale(this);*/
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_blacklist);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FetchDB();
        initInstancesDrawer();

    }

    public void FetchDB(){
        db = new SpamHandler(this);
        blockList = db.getAllBlocks();
    }

    //Tabview
    private void initInstancesDrawer() {
        TabLayout tabs = (TabLayout) findViewById(R.id.tab_layout);
        ViewPager pager = (ViewPager) findViewById(R.id.vpPager);
        adapter = new PagerBlacklistAdapter(getSupportFragmentManager(),this);

        pager.setAdapter(adapter);
        tabs.setupWithViewPager(pager);
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled}, // enabled
                new int[] {-android.R.attr.state_enabled}, // disabled
                new int[] {-android.R.attr.state_checked}, // unchecked
                new int[] { android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[] {
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
                MainActivity.conversationList.clear();
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

    public void update_frg_1(String number){
        Fragment_Conv_Blacklist item = (Fragment_Conv_Blacklist) adapter.getItem(0);
        if (item != null) {
            item.update_recovery_list(number);
        }
    }
    public void update_frg_2(String number){
        Fragment_Blacklist item = (Fragment_Blacklist) adapter.getItem(1);
        if (item != null) {
            item.update_list_phone(number);
        }
    }
}
