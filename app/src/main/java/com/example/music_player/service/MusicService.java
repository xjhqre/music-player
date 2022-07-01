package com.example.music_player.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.danikula.videocache.HttpProxyCacheServer;
import com.example.music_player.ui.activity.MusicPlayActivity;
import com.example.music_player.application.MyApplication;
import com.example.music_player.model.MusicBean;
import com.example.music_player.utils.ActivityUtils;

import java.io.Serializable;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

//这是一个Service服务类
public class MusicService extends Service {

    private static final String TAG = "";
    //声明一个MediaPlayer引用
    private MediaPlayer player;

    //声明一个计时器引用
    private Timer timer;

    MyApplication app;

    //构造函数
    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MusicControl();
    }

    //Binder是一种跨进程的通信方式
    public class MusicControl extends Binder implements Serializable{

        public void play(MusicBean musicBean) {//String path
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                //重置音乐播放器
                player.reset();

                String musicPath = musicBean.getPath();

                // 网络音乐设置文件缓存
//                if (app.getMusicBean().getType() == 1) {
//                    if (app.getMusicBean().getProxyUrl() != null) {
//                        Log.d(TAG, "play: 使用本地缓存地址：" + app.getMusicBean().getProxyUrl());
//                        musicPath = app.getMusicBean().getProxyUrl();
//                    }
//                }

                //加载多媒体文件
                player.setDataSource(musicPath);
                MyApplication.getInstance().setMusicPrepared(false);
                player.prepareAsync();
                // 设置音频流的类型
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //下面的暂停继续和退出方法全部调用的是MediaPlayer自带的方法
        public void pausePlay() {
            player.pause();//暂停播放音乐
        }

        public void continuePlay() {
            player.start();//继续播放音乐
        }

        public void seekTo(int progress) {
            player.seekTo(progress);//设置音乐的播放位置
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        app = (MyApplication) getApplication();

        //创建音乐播放器对象
        player = new MediaPlayer();

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                MyApplication.getInstance().setMusicPrepared(true);
                Log.d(TAG, "onPrepared: 音乐文件准备好了");
                player.start();//播放音乐
                addTimer();//添加计时器
            }
        });

        // 监听音乐播放完成
        player.setOnCompletionListener(mediaPlayer -> {

            // 通知MusicListActivity刷新界面
            if (!ActivityUtils.isDestroy(app.getMusicListActivity())) {
                app.getMusicListActivity().handler.sendEmptyMessage(1);
            }
            if (!ActivityUtils.isDestroy(app.getHomeActivity())) {
                app.getHomeActivity().handler.sendEmptyMessage(1);
            }

            // 自动播放功能
            switch (app.getPlay_order()) {
                case ORDER:
                    app.setCur_position(app.getCur_position() + 1);
                    break;
                case RANDOM:
                    Random random = new Random();
                    app.setCur_position(random.nextInt(app.getPlaylist().size()));
                    break;
                default:
                    break;
            }

            if (app.getCur_position() >= app.getPlaylist().size()) {
                app.setCur_position(0);
            }

            app.setMusicBean(app.getPlaylist().get(app.getCur_position()));

            app.setOver(true);

            app.getMusicControl().play(app.getMusicBean());
        });
    }

    //添加计时器用于设置音乐播放器中的播放进度条
    public void addTimer() {
        //如果timer不存在，也就是没有引用实例
        if (timer == null) {
            //创建计时器对象
            timer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    if (player == null) return;
//                    int duration = player.getDuration();//获取歌曲总时长
                    int duration = app.getMusicBean().getDuration();
                    int currentPosition = player.getCurrentPosition();//获取播放进度
                    Message msg = MusicPlayActivity.handler.obtainMessage();//创建消息对象
                    //将音乐的总时长和播放进度封装至bundle中
                    Bundle bundle = new Bundle();
                    bundle.putInt("duration", duration);
                    bundle.putInt("currentPosition", currentPosition);
                    //再将bundle封装到msg消息对象中
                    msg.setData(bundle);
                    //最后将消息发送到主线程的消息队列
                    MusicPlayActivity.handler.sendMessage(msg);
                }
            };
            //开始计时任务后的5毫秒，第一次执行task任务，以后每500毫秒（0.5s）执行一次
            timer.schedule(task, 5, 500);
        }
    }

    //销毁多媒体播放器
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: MusicService被销毁");
    }

    private void stopMusic() {
        /* 停止音乐的函数*/
        if (player!=null) {
            player.pause();
            player.seekTo(0);
            player.stop();
            player.release();//释放占用的资源
            player = null;//将player置为空
        }

    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind: MusicSerService解绑");
        stopMusic();
        return super.onUnbind(intent);
    }
}

