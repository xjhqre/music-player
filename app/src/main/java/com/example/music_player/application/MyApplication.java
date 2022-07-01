package com.example.music_player.application;

import android.Manifest;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.danikula.videocache.HttpProxyCacheServer;
import com.example.music_player.R;
import com.example.music_player.model.MusicBean;
import com.example.music_player.model.MusicSheet;
import com.example.music_player.model.PlayOrder;
import com.example.music_player.model.User;
import com.example.music_player.service.MusicService;
import com.example.music_player.ui.activity.MusicListActivity;
import com.example.music_player.ui.activity.MusicPlayActivity;
import com.example.music_player.ui.activity.HomeActivity;
import com.example.music_player.ui.activity.NetMusicSearchActivity;
import com.example.music_player.utils.DBOpenHelper;
import com.example.music_player.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;


/**
 * 通过Application使多个activity共享数据
 */
public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private MusicService.MusicControl musicControl; // 音乐播放控制器

    private PlayOrder play_order = PlayOrder.ORDER;  // 默认为顺序播放

    private int cur_position = -1;    // 当前歌曲下标

    // 歌单信息
    ArrayList<MusicSheet> musicSheets = new ArrayList<>();
    ArrayList<MusicBean> localMusicLists = new ArrayList<>(); // 本地音乐集合
    ArrayList<MusicBean> netMusicLists = new ArrayList<>(); // 我喜欢
    ArrayList<MusicBean> curMusicLists = new ArrayList<>(); // 我喜欢
    ArrayList<MusicBean> playlist = new ArrayList<>(); // 播放列表

    String curSheetTitle; // 当前歌单名称

    MusicBean musicBean;

    Bitmap defaultBitmap; // 默认专辑封面

    MyServiceConn conn;  // 连接

    DBOpenHelper dbOpenHelper; // 数据库

    boolean musicPrepared; // 音乐是否准备完成

    // ACtivity对象
    MusicListActivity musicListActivity;
    MusicPlayActivity musicPlayActivity;
    HomeActivity homeActivity;
    NetMusicSearchActivity netMusicSearchActivity;

    // 判断元素
    private boolean isPause = true;  // 歌曲是否暂停
    private boolean isSame = false; // 两次点击的歌曲是否是同一个
    private boolean isOver = false; // 判断歌曲是否播放完成

    // 用户信息
    User user;

    private static Context context;

    private HttpProxyCacheServer proxy; // 缓存

    private static MyApplication app;


    public static HttpProxyCacheServer getProxy() {
        return getInstance().proxy == null ? (getInstance().proxy = getInstance().newProxy()) : getInstance().proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .cacheDirectory(getVedioFile())//缓存的文件夹，默认就不写
                .maxCacheSize(512 * 1024 * 1024)       // 512mb 最大大小，也可为最大个数
                .build();
    }

    public File getVedioFile() {
        String path = getExternalCacheDir() + "/cloudvideo";

        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
        }

        return file;

    }

    public static Context getContext() {
        return context;
    }

    public static MyApplication getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        context = getApplicationContext();

        // 初始化服务
        initService();

        // 初始化工具类
        initUtils();

    }

    // 初始化服务
    private void initService() {
        //创建一个意图对象，是从当前的Activity跳转到Service
        Intent intent = new Intent(this, MusicService.class);
        conn = new MyServiceConn();//创建服务连接对象
        bindService(intent, conn, BIND_AUTO_CREATE);//绑定服务
    }

    private void initUtils() {
        // 默认专辑封面
        defaultBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.wall0);
        dbOpenHelper = new DBOpenHelper(this);
        ToastUtils.init(context);
    }

    //用于实现连接服务，比较模板化，不需要详细知道内容
    class MyServiceConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            System.out.println("------Service Connected-------");
            setMusicControl((MusicService.MusicControl) service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.out.println("------Service DisConnected-------");
        }
    }

    //判断服务是否被解绑
    private void unbind() {
        unbindService(conn);//解绑服务
    }

    @Override
    public void onTerminate() {
        unbind();//解绑服务
        System.out.println("MyApplication终止");
        super.onTerminate();
    }

    public MusicService.MusicControl getMusicControl() {
        return musicControl;
    }

    public void setMusicControl(MusicService.MusicControl musicControl) {
        this.musicControl = musicControl;
    }

    public PlayOrder getPlay_order() {
        return play_order;
    }

    public void setPlay_order(PlayOrder play_order) {
        this.play_order = play_order;
    }

    public int getCur_position() {
        return cur_position;
    }

    public void setCur_position(int cur_position) {
        this.cur_position = cur_position;
    }

    public ArrayList<MusicBean> getLocalMusicLists() {
        return localMusicLists;
    }

    public void setLocalMusicLists(ArrayList<MusicBean> localMusicLists) {
        this.localMusicLists = localMusicLists;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public boolean isSame() {
        return isSame;
    }

    public void setSame(boolean same) {
        isSame = same;
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }

    public MusicBean getMusicBean() {
        return musicBean;
    }

    public void setMusicBean(MusicBean musicBean) {
        this.musicBean = musicBean;
    }

    public MusicListActivity getMusicListActivity() {
        return musicListActivity;
    }

    public void setMusicListActivity(MusicListActivity musicListActivity) {
        this.musicListActivity = musicListActivity;
    }

    public MusicPlayActivity getMusicPlayActivity() {
        return musicPlayActivity;
    }

    public void setMusicPlayActivity(MusicPlayActivity musicPlayActivity) {
        this.musicPlayActivity = musicPlayActivity;
    }

    public MyServiceConn getConn() {
        return conn;
    }

    public void setConn(MyServiceConn conn) {
        this.conn = conn;
    }

    public HomeActivity getHomeActivity() {
        return homeActivity;
    }

    public void setHomeActivity(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<MusicSheet> getMusicSheets() {
        return musicSheets;
    }

    public void setMusicSheets(ArrayList<MusicSheet> musicSheets) {
        this.musicSheets = musicSheets;
    }

    public ArrayList<MusicBean> getNetMusicLists() {
        return netMusicLists;
    }

    public void setNetMusicLists(ArrayList<MusicBean> netMusicLists) {
        this.netMusicLists = netMusicLists;
    }

    public ArrayList<MusicBean> getPlaylist() {
        return playlist;
    }

    public void setPlaylist(ArrayList<MusicBean> playlist) {
        this.playlist = playlist;
    }

    public Bitmap getDefaultBitmap() {
        return defaultBitmap;
    }

    public void setDefaultBitmap(Bitmap defaultBitmap) {
        this.defaultBitmap = defaultBitmap;
    }

    public String getCurSheetTitle() {
        return curSheetTitle;
    }

    public void setCurSheetTitle(String curSheetTitle) {
        this.curSheetTitle = curSheetTitle;
    }

    public ArrayList<MusicBean> getCurMusicLists() {
        return curMusicLists;
    }

    public void setCurMusicLists(ArrayList<MusicBean> curMusicLists) {
        this.curMusicLists = curMusicLists;
    }

    public DBOpenHelper getDbOpenHelper() {
        return dbOpenHelper;
    }

    public void setDbOpenHelper(DBOpenHelper dbOpenHelper) {
        this.dbOpenHelper = dbOpenHelper;
    }

    public boolean isMusicPrepared() {
        return musicPrepared;
    }

    public void setMusicPrepared(boolean musicPrepared) {
        this.musicPrepared = musicPrepared;
    }

    public NetMusicSearchActivity getNetMusicSearchActivity() {
        return netMusicSearchActivity;
    }

    public void setNetMusicSearchActivity(NetMusicSearchActivity netMusicSearchActivity) {
        this.netMusicSearchActivity = netMusicSearchActivity;
    }
}
