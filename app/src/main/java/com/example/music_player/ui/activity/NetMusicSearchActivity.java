package com.example.music_player.ui.activity;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danikula.videocache.HttpProxyCacheServer;
import com.example.music_player.R;
import com.example.music_player.adapter.NetMusicAdapter;
import com.example.music_player.application.MyApplication;
import com.example.music_player.model.MusicBean;
import com.example.music_player.receiver.ExitAppReceiver;
import com.example.music_player.utils.ImageUtils;
import com.example.music_player.utils.NetEaseApiUtils;
import com.example.music_player.utils.ToastUtils;
import com.flyco.systembar.SystemBarHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetMusicSearchActivity extends AppCompatActivity {

    // 视图控件
    @BindView(R.id.activity_net_music_search_recycler_view)
    RecyclerView mRecyclerView;       // 音乐列表View

    @BindView(R.id.activity_net_music_search_toolbar)
    Toolbar mToolbar;

    private NetMusicAdapter adapter; // 适配器

    ArrayList<MusicBean> searchMusicArrayList = new ArrayList<>();

    Intent intent;

    MyApplication app;

    private ExitAppReceiver mExitAppReceiver; // 关闭Activity广播

    ContentResolver resolver;

    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_music_search);

        ButterKnife.bind(this);

        // 注册退出广播接收器
        mExitAppReceiver = new ExitAppReceiver(this);
        registerReceiver(mExitAppReceiver,new IntentFilter("action.exit"));

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 设置返回按钮
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());

        SystemBarHelper.immersiveStatusBar(this, 0);
        SystemBarHelper.setHeightAndPadding(this, mToolbar);

        Intent intent = getIntent();
        query = intent.getStringExtra("song_name");

        // 加载本地数据源
        resolver = getContentResolver();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        app = MyApplication.getInstance();

        app.setNetMusicSearchActivity(this);

        // http请求查询音乐信息
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    NetEaseApiUtils.searchMusic(query, searchMusicArrayList);
                    // 通知主线程封装对象完毕
                    mHandler.sendEmptyMessage(0);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // 歌曲列表适配器设置
        initAdapter();
    }


    // 适配器设置
    private void initAdapter() {
        // 创建适配器对象
        adapter = new NetMusicAdapter(this, searchMusicArrayList);
        mRecyclerView.setAdapter(adapter);

        /* 设置每一项的点击事件*/
        adapter.setOnItemClickListener((view, position) -> {

            app.setMusicBean(searchMusicArrayList.get(position));
            app.getPlaylist().add(0, searchMusicArrayList.get(position));
            app.setCur_position(0);

            // 传递给MusicFragment的数据
            intent = new Intent(this, MusicPlayActivity.class);
            startActivity(intent);
        });
    }

    // 更新recycleView
    public Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                if (searchMusicArrayList.size() != 0) {
                    ToastUtils.show("搜索成功");
                } else {
                    ToastUtils.show("未查询到该歌曲");
                }
                adapter.notifyDataSetChanged();

            }
        }
    };

    @Override
    public void finish() {
        // 通知MusicListActivity刷新界面
        if (app.getMusicListActivity() != null) {
            app.getMusicListActivity().handler.sendEmptyMessage(1);
        }
        super.finish();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(mExitAppReceiver);
        System.out.println("NetMusicSearchActivity被销毁");
    }
}