package com.example.music_player.ui.activity;

import android.annotation.SuppressLint;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.ViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.model.PlayOrder;
import com.example.music_player.receiver.ExitAppReceiver;
import com.example.music_player.utils.ActivityUtils;
import com.example.music_player.utils.ImageUtils;
import com.example.music_player.utils.NetEaseApiUtils;
import com.example.music_player.utils.ToastUtils;
import com.example.music_player.widget.AlbumCoverView;
import com.flyco.systembar.SystemBarHelper;

import org.json.JSONException;

import java.io.IOException;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;

public class MusicPlayActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MusicPlayActivity";
    //进度条
    private static SeekBar music_sb;
    private static TextView tv_progress;
    private static TextView tv_total;
    private static TextView music_song;
    private static TextView music_author;

    // 按钮
    ImageButton music_next_music_btn;       // 下一曲按钮
    ImageButton music_play_music_btn;       // 播放按钮
    ImageButton music_last_music_btn;       // 上一曲按钮
    ImageButton music_OrderSelectionBtn;    // 播放顺序选择按钮

    //动画
    ImageView iv_back_button;         // 专辑封面视图、回退按钮
    private ImageView iv_music_background;            // 音乐播放界面高斯模糊背景图

    @BindView(R.id.activity_music_title_RL)
    RelativeLayout activity_music_title_RL;

    @BindView(R.id.album_cover_view)
    AlbumCoverView mAlbumCoverView;

    private ExitAppReceiver mExitAppReceiver; // 关闭Activity广播

    MyApplication app;

    CustomViewTarget target;

    MultiTransformation<Bitmap> blurTransformation;
    MultiTransformation<Bitmap> circleTransformation;

    // 音乐图片
    Object cover;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        ButterKnife.bind(this);

        target = new CustomViewTarget<AlbumCoverView, Drawable>(mAlbumCoverView) {

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {

            }

            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                Log.d(TAG, "onResourceReady: 动画加载成功");
                mAlbumCoverView.setCoverBitmap(ImageUtils.DrawableToBitmap(resource));
                if (!app.isPause()) {
                    mAlbumCoverView.start();
                } else {
                    mAlbumCoverView.pause();
                }
            }

            @Override
            protected void onResourceCleared(@Nullable Drawable placeholder) {

            }
        };

        // 图片高斯模糊转换
        blurTransformation = new MultiTransformation<>(new BlurTransformation(25, 3), new VignetteFilterTransformation(new PointF(0.5f, 0.5f), new float[]{0.0f, 0.0f, 0.0f}, 0.0f, 1f));
        circleTransformation = new MultiTransformation<>(new CircleCrop(), new CropTransformation(AlbumCoverView.roundLength, AlbumCoverView.roundLength));

        SystemBarHelper.immersiveStatusBar(this, 0);
        SystemBarHelper.setHeightAndPadding(this, activity_music_title_RL);

        app = MyApplication.getInstance();
        app.setMusicPlayActivity(this);

        // 注册退出广播接收器
        mExitAppReceiver = new ExitAppReceiver(this);
        registerReceiver(mExitAppReceiver, new IntentFilter("action.exit"));

        // 初始化控件
        initView();

        // 初始化参数
        initParam();

        // 初始化监听器
        initListener();

        // 初始化音乐播放界面信息
        initMusicInf();

        // 初始化指针视图
        mAlbumCoverView.initNeedle(!app.isPause());

        // 跳转过来后自动播放音乐
        if (!app.isSame()) {
            app.getMusicControl().play(app.getMusicBean());
            music_play_music_btn.setBackgroundResource(R.drawable.ic_pause);
            app.setPause(false);
        } else {
            app.setSame(false);
            // 如果是暂停状态
            if (app.isPause()) {
                music_play_music_btn.setBackgroundResource(R.drawable.ic_play);
            } else {
                music_play_music_btn.setBackgroundResource(R.drawable.ic_pause);
            }
        }

        // 初始化动画效果和高斯模糊背景
        initAnime();

    }


    // 初始化动画效果和高斯模糊背景
    private void initAnime() {

        if (app.getMusicBean().getType() == 0) {
            System.out.println(app.getMusicBean().getAlbumArt());
            cover = BitmapFactory.decodeFile(app.getMusicBean().getAlbumArt());
        } else {
            cover = app.getMusicBean().getAlbumArt();
        }

        if (cover == null) {
            cover = app.getDefaultBitmap();
        }

        Glide.with(this).load(cover)
                .apply(RequestOptions.bitmapTransform(blurTransformation))
                .into(iv_music_background);

        Glide.with(this).load(cover)
                .apply(RequestOptions.bitmapTransform(circleTransformation))
                .into(target);

    }

    // 初始化参数
    private void initParam() {

        // 更新播放顺序图标
        switch (app.getPlay_order()) {
            case ORDER:
                music_OrderSelectionBtn.setBackgroundResource(R.drawable.ic_order);
                break;
            case RANDOM:
                music_OrderSelectionBtn.setBackgroundResource(R.drawable.ic_random);
                break;
            case ONLY:
                music_OrderSelectionBtn.setBackgroundResource(R.drawable.ic_only);
                break;
            default:
                break;
        }

        // 更新当前音乐对象
        app.setMusicBean(app.getPlaylist().get(app.getCur_position()));
    }

    // 初始化音乐播放界面信息以及旋转动画
    private void initMusicInf() {

        // 设置歌曲名称和作者
        music_song.setText(app.getMusicBean().getTitle());
        music_author.setText(app.getMusicBean().getArtist());

    }

    // 初始化监听器
    private void initListener() {

        music_play_music_btn.setOnClickListener(this);
        music_next_music_btn.setOnClickListener(this);
        music_last_music_btn.setOnClickListener(this);
        iv_back_button.setOnClickListener(this);

        // TODO 显示播放顺序更改信息
        music_OrderSelectionBtn.setOnClickListener(view -> {
            switch (app.getPlay_order()) {
                case ORDER:
                    music_OrderSelectionBtn.setBackgroundResource(R.drawable.ic_random);
                    app.setPlay_order(PlayOrder.RANDOM);
                    ToastUtils.show("随机播放");
                    break;
                case RANDOM:
                    music_OrderSelectionBtn.setBackgroundResource(R.drawable.ic_only);
                    app.setPlay_order(PlayOrder.ONLY);
                    ToastUtils.show("单曲循环");
                    break;
                case ONLY:
                    music_OrderSelectionBtn.setBackgroundResource(R.drawable.ic_order);
                    app.setPlay_order(PlayOrder.ORDER);
                    ToastUtils.show("顺序播放");
                    break;
                default:
                    break;

            }
        });

        // 进度条添加事件监听
        music_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            //这一行注解是保证API在KITKAT以上的模拟器才能顺利运行，也就是19以上
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                if (app.isOver()) {
                    // 刷新图片动画
                    initAnime();

                    // 刷新歌曲信息
                    initMusicInf();

                    app.setOver(false);
                }

            }

            @Override
            //滑动条开始滑动时调用
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            //滑动条停止滑动时调用
            public void onStopTrackingTouch(SeekBar seekBar) {
                //根据拖动的进度改变音乐播放进度
                int progress = seekBar.getProgress();//获取seekBar的进度
                app.getMusicControl().seekTo(progress);//改变播放进度
            }
        });
    }

    // 初始化控件
    private void initView() {

        // 绑定音乐播放界面背景控件
        iv_music_background = findViewById(R.id.activity_music_background);

        //进度条上小绿点的位置，也就是当前已播放时间
        tv_progress = findViewById(R.id.tv_progress);

        //进度条的总长度，就是总时间
        tv_total = findViewById(R.id.tv_total);

        //进度条的控件
        music_sb = findViewById(R.id.sb);

        //歌曲名显示的控件
        music_song = findViewById(R.id.song_name);
        music_author = findViewById(R.id.song_author);

        //绑定控件的同时设置点击事件监听器
        music_play_music_btn = findViewById(R.id.music_play_btn);
        music_next_music_btn = findViewById(R.id.btn_next);
        music_last_music_btn = findViewById(R.id.btn_back);
        music_OrderSelectionBtn = findViewById(R.id.playOrderSelectionButtons);
        iv_back_button = findViewById(R.id.dropDownButton);
    }

    // 接收歌曲进度信息，显示进度条
    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler(Looper.getMainLooper()) {

        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg) {

            if (ActivityUtils.isDestroy(MyApplication.getInstance().getMusicPlayActivity())) {
                return;
            }

            Bundle bundle = msg.getData();//获取从子线程发送过来的音乐播放进度

            //获取当前进度currentPosition和总时长duration
            int duration = bundle.getInt("duration");
            int currentPosition = bundle.getInt("currentPosition");

            if (music_sb == null) return;

            //对进度条进行设置
            music_sb.setMax(duration);
            music_sb.setProgress(currentPosition);

            //歌曲是多少分钟多少秒钟
            int minute = duration / 1000 / 60;
            int second = duration / 1000 % 60;
            String strMinute = null;
            String strSecond = null;
            if (minute < 10) {//如果歌曲的时间中的分钟小于10
                strMinute = "0" + minute;//在分钟的前面加一个0
            } else {
                strMinute = minute + "";
            }
            if (second < 10) {//如果歌曲中的秒钟小于10
                strSecond = "0" + second;//在秒钟前面加一个0
            } else {
                strSecond = second + "";
            }

            //这里就显示了歌曲总时长
            tv_total.setText(strMinute + ":" + strSecond);

            //歌曲当前播放时长
            minute = currentPosition / 1000 / 60;
            second = currentPosition / 1000 % 60;
            if (minute < 10) {//如果歌曲的时间中的分钟小于10
                strMinute = "0" + minute;//在分钟的前面加一个0
            } else {
                strMinute = minute + " ";
            }
            if (second < 10) {//如果歌曲中的秒钟小于10
                strSecond = "0" + second;//在秒钟前面加一个0
            } else {
                strSecond = second + " ";
            }
            //显示当前歌曲已经播放的时间
            tv_progress.setText(strMinute + ":" + strSecond);
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.music_play_btn://播放按钮点击事件

                // 是是暂停状态，需要继续播放
                if (app.isPause()) {
                    app.getMusicControl().continuePlay();
                    music_play_music_btn.setBackgroundResource(R.drawable.ic_pause);
//                    animator.resume();
                    app.setPause(false);
                    mAlbumCoverView.start();
                }
                // 不是暂停状态，需要暂停
                else {
                    app.getMusicControl().pausePlay();
                    music_play_music_btn.setBackgroundResource(R.drawable.ic_play);
//                    animator.pause();
                    app.setPause(true);
                    mAlbumCoverView.pause();
                }
                break;

            case R.id.btn_back: // 切换上一曲
                switchToPreviousSong();
                break;

            case R.id.btn_next: // 切换下一曲
                switchToNextSong();
                break;

            case R.id.dropDownButton: // 回到歌单界面

                onBackPressed();
                break;

            default:
                break;
        }
    }

    // 切换上一首歌
    private void switchToPreviousSong() {
        app.setCur_position(app.getCur_position() - 1);
        if (app.getCur_position() < 0) {
            app.setCur_position(app.getPlaylist().size() - 1);
        }

        app.setMusicBean(app.getPlaylist().get(app.getCur_position()));

        app.setPause(false);
        music_play_music_btn.setBackgroundResource(R.drawable.ic_pause);

        // 刷新图片动画
        initAnime();

        // 刷新歌曲信息
        initMusicInf();

        app.getMusicControl().play(app.getMusicBean());

    }

    // 切换下一首歌
    public void switchToNextSong() {

        switch (app.getPlay_order()) {
            case RANDOM:
                Random random = new Random();
                app.setCur_position(random.nextInt(app.getPlaylist().size()));
                break;
            default:
                app.setCur_position(app.getCur_position() + 1);
                break;
        }

        if (app.getCur_position() >= app.getPlaylist().size()) {
            app.setCur_position(0);
        }
        app.setMusicBean(app.getPlaylist().get(app.getCur_position()));

        music_play_music_btn.setBackgroundResource(R.drawable.ic_pause);
        app.setPause(false);

        // 刷新图片动画
        initAnime();

        // 刷新歌曲信息
        initMusicInf();

        app.getMusicControl().play(app.getMusicBean());
    }

    @Override
    public void finish() {
        // 通知MusicListActivity刷新界面
        if (!ActivityUtils.isDestroy(app.getMusicListActivity())) {
            app.getMusicListActivity().handler.sendEmptyMessage(1);
        }
        if (!ActivityUtils.isDestroy(app.getHomeActivity())) {
            app.getHomeActivity().handler.sendEmptyMessage(1);
        }
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(mExitAppReceiver);
        System.out.println("MusicPlayActivity销毁");
    }
}