package com.example.music_player.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import jp.wasabeef.glide.transformations.BlurTransformation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_player.R;
import com.example.music_player.adapter.MusicAdapter;
import com.example.music_player.application.MyApplication;
import com.example.music_player.receiver.ExitAppReceiver;
import com.example.music_player.utils.ActivityUtils;
import com.example.music_player.utils.ImageUtils;
import com.flyco.systembar.SystemBarHelper;
import com.google.android.material.appbar.AppBarLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;


public class MusicListActivity extends AppCompatActivity {
    // 视图控件
    @BindView(R.id.local_music_rv)
    RecyclerView main_rv_musicList;              // 音乐列表View

    @BindView(R.id.activity_music_bottom_icon)
    ImageView main_iv_music_icon;           // 信息栏里的音乐图片

    @BindView(R.id.main_play_btn)
    ImageButton main_iv_play;               // 播放按钮

    @BindView(R.id.local_music_bottom_tv_song)
    TextView main_tv_song;         // 歌曲名

    @BindView(R.id.local_music_bottom_tv_singer)
    TextView main_tv_singer;       // 歌手名

    @BindView(R.id.local_music_bottom_layout)
    RelativeLayout local_music_bottom_layout;    //主界面下方信息栏

    @BindView(R.id.activity_music_list_pic)
    ImageView activity_music_list_pic;

    @BindView(R.id.activity_music_list_blur_background)
    ImageView activity_music_list_blur_background;

    @BindView(R.id.music_list_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.music_list_appbar)
    AppBarLayout music_list_appbar;


    private MusicAdapter adapter; // 适配器

    Intent it1;

    MyApplication app;

    Bitmap bm;

    private ExitAppReceiver mExitAppReceiver; // 关闭Activity广播


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);

        ButterKnife.bind(this);

        app = MyApplication.getInstance();
        app.setMusicListActivity(this);

        // 注册退出广播接收器
        mExitAppReceiver = new ExitAppReceiver(this);
        registerReceiver(mExitAppReceiver,new IntentFilter("action.exit"));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(app.getCurSheetTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 设置返回按钮
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
        music_list_appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset <= -music_list_appbar.getHeight() / 2) {
                    activity_music_list_pic.setVisibility(View.INVISIBLE);
                } else {
                    activity_music_list_pic.setVisibility(View.VISIBLE);
                }
            }
        });
        SystemBarHelper.immersiveStatusBar(this, 0);
        SystemBarHelper.setHeightAndPadding(this, mToolbar);


        main_rv_musicList.setLayoutManager(new LinearLayoutManager(this));


        // 初始化界面信息
        initMusicListActivity();

        // 设置信息栏监听事件
        setViewListener();

        // 歌曲列表适配器设置
        initAdapter();

    }

    // 初始化MusicListActivity信息
    private void initMusicListActivity() {

        MultiTransformation<Bitmap> bitmapMultiTransformation = new MultiTransformation<>(new BlurTransformation(25, 3), new VignetteFilterTransformation(new PointF(0.5f, 0.5f), new float[]{0.0f, 0.0f, 0.0f}, 0.0f, 1f));

        // *****************************加载上方信息******************************
        // 使用默认背景
        if (app.getCurMusicLists().size() == 0) {
            Glide.with(this).load(app.getDefaultBitmap()).override(200, 200).into(activity_music_list_pic);

            Glide.with(this).load(app.getDefaultBitmap()).apply(RequestOptions.bitmapTransform(bitmapMultiTransformation)).into(activity_music_list_blur_background);
        }
        else if (app.getCurMusicLists().get(0).getType() == 1){  // 加载网络音乐图片
            Glide.with(this).load(app.getCurMusicLists().get(0).getAlbumArt()).override(200, 200).into(activity_music_list_pic);
            // 高斯模糊背景
            Glide.with(this).load(app.getCurMusicLists().get(0).getAlbumArt()).apply(RequestOptions.bitmapTransform(bitmapMultiTransformation)).into(activity_music_list_blur_background);
        }
        else {   // 加载本地图片
            bm = BitmapFactory.decodeFile(app.getCurMusicLists().get(0).getAlbumArt());
            Glide.with(this).load(bm).override(200, 200).into(activity_music_list_pic);
            // 高斯模糊背景
            Glide.with(this).load(bm).apply(RequestOptions.bitmapTransform(bitmapMultiTransformation)).into(activity_music_list_blur_background);
        }


        // *************************************加载下方信息****************************************
        if (app.getMusicBean() != null) {
            local_music_bottom_layout.setVisibility(View.VISIBLE);

            // 按钮
            main_tv_song.setText(app.getMusicBean().getTitle());
            main_tv_singer.setText(app.getMusicBean().getArtist());
            if (app.isPause()) {
                main_iv_play.setBackgroundResource(R.drawable.ic_main_play);
            } else {
                main_iv_play.setBackgroundResource(R.drawable.ic_main_pause);
            }

            // 音乐封面
            if (app.getMusicBean().getType() == 0) {  // 本地音乐
                bm = BitmapFactory.decodeFile(app.getMusicBean().getAlbumArt());
                if (bm == null) {
                    bm = app.getDefaultBitmap();
                }
                Glide.with(this).load(bm).override(200, 200).into(main_iv_music_icon);
            } else if (app.getMusicBean().getType() == 1) {   // 网络音乐
//                bm = app.getMusicBean().getAlbumPic();
                Glide.with(this).load(app.getMusicBean().getAlbumArt()).override(200, 200).into(main_iv_music_icon);
            }

//            main_iv_music_icon.setImageBitmap(bm);
        } else {
            local_music_bottom_layout.setVisibility(View.GONE);
        }
    }

    // 设置监听事件
    private void setViewListener() {

        // 点击主界面下方信息栏跳转到播发界面
        local_music_bottom_layout.setOnClickListener(view -> {
            if (app.getCur_position() == -1) {
                app.setCur_position(0);
            } else {
                app.setSame(true);
            }
            it1 = new Intent(this, MusicPlayActivity.class);
            startActivity(it1);

        });

        // 主界面下方信息栏播放按钮
        main_iv_play.setOnClickListener(view -> {

            // 当前是暂停状态，转换到播放状态
            if (app.isPause()) {
                main_iv_play.setBackgroundResource(R.drawable.ic_main_pause);
                app.getMusicControl().continuePlay();
                app.setPause(false);

            } else {
                main_iv_play.setBackgroundResource(R.drawable.ic_main_play);
                app.getMusicControl().pausePlay();
                app.setPause(true);
            }

//            }
        });
    }

    // 适配器设置
    private void initAdapter() {
        // 创建适配器对象
        adapter = new MusicAdapter(this, app.getCurMusicLists());
        main_rv_musicList.setAdapter(adapter);

        /* 设置每一项的点击事件*/
        adapter.setOnItemClickListener((view, position) -> {

            app.getPlaylist().clear();
            app.getPlaylist().addAll(app.getCurMusicLists());
            app.setCur_position(-1);

            // 判断是否点击的是同一曲
//            app.setSame(app.getCur_position() == position);
            if (app.getMusicBean() != null) {
                app.setSame(app.getMusicBean().getSongId().equals(app.getPlaylist().get(position).getSongId()));
            }

            app.setCur_position(position);

            // 传递给MusicFragment的数据
            it1 = new Intent(this, MusicPlayActivity.class);

            startActivity(it1);
        });
    }


    // 接收消息，刷新界面
    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler(Looper.getMainLooper()) {

        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg) {

            if (ActivityUtils.isDestroy(app.getMusicListActivity())) {
                return;
            }

            // 刷新页面
            if (msg.what == 1) {
                if (local_music_bottom_layout.getVisibility() == View.GONE) {
                    local_music_bottom_layout.setVisibility(View.VISIBLE);
                }
                // 加载主界面下方歌曲信息栏信息
                if (app.getMusicBean().getType() == 0) {  // 本地音乐
                    String albumArt = app.getPlaylist().get(app.getCur_position()).getAlbumArt();

                    if (albumArt != null) {
                        bm = BitmapFactory.decodeFile(albumArt);

                    } else {
                        bm = app.getDefaultBitmap();
                    }
                    Glide.with(app.getMusicListActivity()).load(bm).override(100, 100).into(main_iv_music_icon);
                } else { // 网络音乐
                    Glide.with(app.getMusicListActivity()).load(app.getMusicBean().getAlbumArt()).override(100, 100).into(main_iv_music_icon);

                }

                main_tv_song.setText(app.getPlaylist().get(app.getCur_position()).getTitle());
                main_tv_singer.setText(app.getPlaylist().get(app.getCur_position()).getArtist());
                if (app.isPause()) {
                    main_iv_play.setBackgroundResource(R.drawable.ic_main_play);
                } else {
                    main_iv_play.setBackgroundResource(R.drawable.ic_main_pause);
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bm != null) {
            bm.recycle();
        }
        // 注销广播接收器
        unregisterReceiver(mExitAppReceiver);
        System.out.println("MusicListActivity被销毁");
    }

}