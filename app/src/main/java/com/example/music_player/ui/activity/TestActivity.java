package com.example.music_player.ui.activity;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.example.music_player.R;
import com.example.music_player.receiver.ExitAppReceiver;

public class TestActivity extends AppCompatActivity {

    private ExitAppReceiver mExitAppReceiver; // 关闭Activity广播

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        FloatingSearchView mSearchView = findViewById(R.id.floating_search_view);

        // 注册退出广播接收器
        mExitAppReceiver = new ExitAppReceiver(this);
        registerReceiver(mExitAppReceiver,new IntentFilter("action.exit"));

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {


            }

        });


        mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {

            }

            @Override
            public void onMenuClosed() {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        ComponentName componentName = new ComponentName(this, SearchableActivity.class);
//        SearchableInfo info = searchManager.getSearchableInfo(componentName);
//        searchView.setSearchableInfo(info);
//        searchView.setIconifiedByDefault(false);
//        searchView.setSubmitButtonEnabled(true);    // 显示“开始搜索”的按钮
//        searchView.setQueryRefinementEnabled(true); // 提示内容右边提供一个将提示内容放到搜索框的按钮
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(mExitAppReceiver);
    }
}
