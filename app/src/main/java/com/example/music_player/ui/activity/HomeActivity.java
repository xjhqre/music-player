package com.example.music_player.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.bumptech.glide.Glide;
import com.example.music_player.application.MyApplication;
import com.example.music_player.receiver.ExitAppReceiver;
import com.example.music_player.ui.fragment.MusicFoundFragment;
import com.example.music_player.ui.fragment.MusicSheetFragment;
import com.example.music_player.utils.ToastUtils;
import com.flyco.systembar.SystemBarHelper;
import com.example.music_player.R;
import com.google.android.material.navigation.NavigationView;


import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.found_music)
    TextView found_music_btn;

    @BindView(R.id.boke)
    TextView boke_btn;

    @BindView(R.id.music_sheet)
    TextView music_sheet_btn;

    @BindView(R.id.follow)
    TextView follow_btn;

    @BindView(R.id.voice)
    TextView voice_btn;

    @BindView(R.id.activity_main_toolbar)
    Toolbar activity_main_toolbar;

    @BindView(R.id.activity_main_music_inf_bar)
    LinearLayout activity_main_music_inf_bar;

    @BindView(R.id.activity_main_music_inf_bar_music_pic)
    ImageView activity_main_music_inf_bar_music_pic;

    @BindView(R.id.activity_main_music_inf_bar_music_name)
    TextView activity_main_music_inf_bar_music_name;

    @BindView(R.id.activity_main_music_inf_bar_music_singer)
    TextView activity_main_music_inf_bar_music_singer;

    @BindView(R.id.activity_main_music_inf_bar_play_btn)
    ImageButton activity_main_music_inf_bar_play_btn;

    View headerView;
    TextView nav_header_main_username;
    TextView nav_header_main_phone;

    // fragment视图
    private MusicSheetFragment msf;
    private MusicFoundFragment mff;

    private FragmentManager fm;

    MyApplication app;

    Bitmap bm;

    private ExitAppReceiver mExitAppReceiver; // 关闭Activity广播

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        fm = getSupportFragmentManager();

        app = (MyApplication) getApplication();
        app.setHomeActivity(this);

        // 注册退出广播接收器
        mExitAppReceiver = new ExitAppReceiver(this);
        registerReceiver(mExitAppReceiver,new IntentFilter("action.exit"));

        // fragment选择设置
        found_music_btn.setOnClickListener(this);
//        boke_btn.setOnClickListener(this);
        music_sheet_btn.setOnClickListener(this);
//        follow_btn.setOnClickListener(this);
//        voice_btn.setOnClickListener(this);
        mNavigationView.setNavigationItemSelectedListener(this);

        // 设置左边滑动信息栏的用户信息
        headerView = mNavigationView.getHeaderView(0);
        nav_header_main_username = headerView.findViewById(R.id.nav_header_main_username);
        nav_header_main_phone = headerView.findViewById(R.id.nav_header_main_phone);
        nav_header_main_username.setText(app.getUser().getName());
        nav_header_main_phone.setText(app.getUser().getPhonenum());
        SystemBarHelper.immersiveStatusBar(this, 0);
        SystemBarHelper.setPadding(this, activity_main_toolbar);

        // 申请外部存储权限
        int permission_write = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permission_read = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission_write != PackageManager.PERMISSION_GRANTED
                || permission_read != PackageManager.PERMISSION_GRANTED) {
            ToastUtils.show("正在请求权限");
            //申请权限，特征码自定义为1，可在回调时进行相关判断
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // 设置播放音乐按钮监听器
        setMusicPlayBtnListener();

        //模拟一次点击，既进去后选择第一项
        music_sheet_btn.performClick();
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {
            MyApplication.getInstance().sendBroadcast(new Intent("action.exit"));
//            System.exit(0);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void toggleDrawer() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
    }

    //重置所有文本的选中状态
    private void setSelected() {
        found_music_btn.setSelected(false);
        boke_btn.setSelected(false);
        music_sheet_btn.setSelected(false);
        follow_btn.setSelected(false);
        voice_btn.setSelected(false);
        Drawable top1 = getResources().getDrawable(R.drawable.ic_found);
        found_music_btn.setCompoundDrawablesWithIntrinsicBounds(null, top1 , null, null);
        found_music_btn.setTextColor(Color.parseColor("#9a9a9a"));
        Drawable top2 = getResources().getDrawable(R.drawable.ic_music);
        music_sheet_btn.setCompoundDrawablesWithIntrinsicBounds(null, top2 , null, null);
        music_sheet_btn.setTextColor(Color.parseColor("#9a9a9a"));
    }

    //隐藏所有Fragment
    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if (msf != null) fragmentTransaction.hide(msf);
        if (mff != null) fragmentTransaction.hide(mff);
//        if(fg3 != null)fragmentTransaction.hide(fg3);
//        if(fg4 != null)fragmentTransaction.hide(fg4);
    }


    // 按钮栏点击事件
    @Override
    public void onClick(View view) {
        FragmentTransaction fTransaction = fm.beginTransaction();
        hideAllFragment(fTransaction);
        switch (view.getId()) {
            case R.id.found_music:
                setSelected();
                found_music_btn.setSelected(true);
                Drawable top1 = AppCompatResources.getDrawable(this, R.drawable.ic_found_selected);
                found_music_btn.setCompoundDrawablesWithIntrinsicBounds(null, top1 , null, null);
                found_music_btn.setTextColor(Color.parseColor("#FE3A3B"));
                if (mff == null) {
                    mff = new MusicFoundFragment();
                    fTransaction.add(R.id.ly_home_content, mff);
                } else {
                    fTransaction.show(mff);
                }
                break;
            case R.id.music_sheet:
                setSelected();
                music_sheet_btn.setSelected(true);
                Drawable top2 = AppCompatResources.getDrawable(this,R.drawable.ic_music_selected);
                music_sheet_btn.setCompoundDrawablesWithIntrinsicBounds(null, top2 , null, null);
                music_sheet_btn.setTextColor(Color.parseColor("#FE3A3B"));
                if (msf == null) {
                    msf = new MusicSheetFragment();
                    fTransaction.add(R.id.ly_home_content, msf);
                } else {
                    fTransaction.show(msf);
                }
                break;
        }
        fTransaction.commit();
    }

    // 设置播放按钮监听事件
    private void setMusicPlayBtnListener() {

        // 点击主界面下方信息栏跳转到播发界面
        activity_main_music_inf_bar.setOnClickListener(view -> {
            app.setSame(true);
            Intent intent = new Intent(this, MusicPlayActivity.class);
            startActivity(intent);

        });

        // 主界面下方信息栏播放按钮
        activity_main_music_inf_bar_play_btn.setOnClickListener(view -> {
            // 当前是暂停状态，转换到播放状态
            if (app.isPause()) {
                activity_main_music_inf_bar_play_btn.setBackgroundResource(R.drawable.ic_main_pause);
                app.getMusicControl().continuePlay();
                app.setPause(false);

            } else {
                activity_main_music_inf_bar_play_btn.setBackgroundResource(R.drawable.ic_main_play);
                app.getMusicControl().pausePlay();
                app.setPause(true);
            }
        });
    }

    // 接收消息，刷新界面
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(Looper.getMainLooper()) {

        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg) {

            // 刷新页面下方音乐信息栏
            if (msg.what == 1) {
                // 本地音乐
                if (app.getMusicBean().getType() == 0) {
                    String albumArt = app.getMusicBean().getAlbumArt();
                    Bitmap bm;
                    if (albumArt != null) {
                        bm = BitmapFactory.decodeFile(albumArt);

                    } else {
                        bm = app.getDefaultBitmap();
                    }
                    Glide.with(app.getHomeActivity()).load(bm).override(100, 100).into(activity_main_music_inf_bar_music_pic);
                } else { // 网络音乐
                    Glide.with(app.getHomeActivity()).load(app.getMusicBean().getAlbumArt()).override(100, 100).into(activity_main_music_inf_bar_music_pic);
                }

                activity_main_music_inf_bar_music_name.setText(app.getMusicBean().getTitle());
                activity_main_music_inf_bar_music_singer.setText(app.getMusicBean().getArtist());
                if (app.isPause()) {
                    activity_main_music_inf_bar_play_btn.setBackgroundResource(R.drawable.ic_main_play);
                } else {
                    activity_main_music_inf_bar_play_btn.setBackgroundResource(R.drawable.ic_main_pause);
                }
            }

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //权限已成功申请
                }else{
                    //用户拒绝授权
                    ToastUtils.show("无法获取SD卡读写权限");
                    // 退出app
                    MyApplication.getInstance().sendBroadcast(new Intent("action.exit"));
                    System.exit(0);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新下方信息栏
        if (app.getMusicBean() != null) {
            activity_main_music_inf_bar.setVisibility(View.VISIBLE);
            // 加载主界面下方歌曲信息栏信息
            if (app.getMusicBean().getType() == 0) {  // 本地音乐
                bm = BitmapFactory.decodeFile(app.getMusicBean().getAlbumArt());
                if (bm == null) {
                    bm = app.getDefaultBitmap();
                }
                Glide.with(app.getHomeActivity()).load(bm).into(activity_main_music_inf_bar_music_pic);
            } else if (app.getMusicBean().getType() == 1) {   // 网络音乐
                Glide.with(app.getHomeActivity()).load(app.getMusicBean().getAlbumArt()).into(activity_main_music_inf_bar_music_pic);
            }

            activity_main_music_inf_bar_music_name.setText(app.getMusicBean().getTitle());
            activity_main_music_inf_bar_music_singer.setText(app.getMusicBean().getArtist());
            if (app.isPause()) {
                activity_main_music_inf_bar_play_btn.setBackgroundResource(R.drawable.ic_main_play);
            } else {
                activity_main_music_inf_bar_play_btn.setBackgroundResource(R.drawable.ic_main_pause);
            }
        } else {
            activity_main_music_inf_bar.setVisibility(View.GONE);
        }
        System.out.println("onResume");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bm != null) {
            bm.recycle();
        }
        // 注销广播接收器
        unregisterReceiver(mExitAppReceiver);
        System.out.println("HomeActivity被销毁");
    }
}

