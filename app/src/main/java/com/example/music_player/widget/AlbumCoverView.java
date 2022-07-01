package com.example.music_player.widget;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;

import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.utils.ImageUtils;


/**
 * 专辑封面
 * Created by wcy on 2015/11/30.
 */
public class AlbumCoverView extends View implements ValueAnimator.AnimatorUpdateListener {
    private static final long TIME_UPDATE = 30L;  // 旋转速度，越小越快
    private static final float DISC_ROTATION_INCREASE = 0.5f;
    private static final float NEEDLE_ROTATION_PLAY = 0.0f;
    private static final float NEEDLE_ROTATION_PAUSE = -25.0f;
    private Handler mHandler = new Handler();
    private Bitmap mDiscBitmap;
    private Bitmap mCoverBitmap;
    private Bitmap mNeedleBitmap;
    private Drawable mCoverBorder;
    private int mCoverBorderWidth;
    private Matrix mDiscMatrix = new Matrix();
    private Matrix mCoverMatrix = new Matrix();
    private Matrix mNeedleMatrix = new Matrix();
    private ValueAnimator mPlayAnimator;
    private ValueAnimator mPauseAnimator;
    private float mDiscRotation = 0.0f;
    private float mNeedleRotation = NEEDLE_ROTATION_PLAY;
    private boolean isPlaying = false;

    // 图片起始坐标
    private Point mDiscPoint = new Point();
    private Point mCoverPoint = new Point();
    private Point mNeedlePoint = new Point();
    // 旋转中心坐标
    private Point mDiscCenterPoint = new Point();
    private Point mCoverCenterPoint = new Point();
    private Point mNeedleCenterPoint = new Point();

    MyApplication app;

    public static int roundLength;

    Context musicPlayContext;

    public AlbumCoverView(Context context) {
        this(context, null);
    }

    public AlbumCoverView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AlbumCoverView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        musicPlayContext = context;
        roundLength = getScreenWidth() / 2;
        app = (MyApplication) ((Activity) context).getApplication();
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            initOnLayout();
        }
    }

    private void init() {
        // 透明圆
        mCoverBorder = getResources().getDrawable(R.drawable.play_page_cover_border_shape);
        // 黑胶唱片
        mDiscBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.play_page_disc);
        mCoverBitmap = null;
        // 探针
        mNeedleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.play_page_needle);
        mCoverBorderWidth = dp2px(1);

        // 探针动画
        mPlayAnimator = ValueAnimator.ofFloat(NEEDLE_ROTATION_PAUSE, NEEDLE_ROTATION_PLAY);
        mPlayAnimator.setDuration(300);
        mPlayAnimator.addUpdateListener(this);
        mPauseAnimator = ValueAnimator.ofFloat(NEEDLE_ROTATION_PLAY, NEEDLE_ROTATION_PAUSE);
        mPauseAnimator.setDuration(300);
        mPauseAnimator.addUpdateListener(this);
    }

    private void initOnLayout() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (mCoverBitmap == null) {
            mCoverBitmap = ImageUtils.createCircleImage(ImageUtils.resizeImage(app.getDefaultBitmap(), roundLength, roundLength));
        }

        int unit = Math.min(getWidth(), getHeight()) / 8;
        setRoundLength(unit * 4);

        mDiscBitmap = ImageUtils.resizeImage(mDiscBitmap, unit * 6, unit * 6);
        mCoverBitmap = ImageUtils.resizeImage(mCoverBitmap, unit * 4, unit * 4);
        mNeedleBitmap = ImageUtils.resizeImage(mNeedleBitmap, unit * 2, unit * 3);

        int discOffsetY = mNeedleBitmap.getHeight() / 2;
        mDiscPoint.x = (getWidth() - mDiscBitmap.getWidth()) / 2;
        mDiscPoint.y = discOffsetY + mNeedleBitmap.getWidth() / 3;
        mCoverPoint.x = (getWidth() - mCoverBitmap.getWidth()) / 2;
        mCoverPoint.y = discOffsetY + (mDiscBitmap.getHeight() - mCoverBitmap.getHeight()) / 2 + mNeedleBitmap.getWidth() / 3;
        mNeedlePoint.x = getWidth() / 2 - mNeedleBitmap.getWidth() / 6;
        mNeedlePoint.y = mNeedleBitmap.getWidth() / 6;
        mDiscCenterPoint.x = getWidth() / 2;
        mDiscCenterPoint.y = mDiscBitmap.getHeight() / 2 + discOffsetY + mNeedleBitmap.getWidth() / 3;
        mCoverCenterPoint.x = mDiscCenterPoint.x;
        mCoverCenterPoint.y = mDiscCenterPoint.y;
        mNeedleCenterPoint.x = mDiscCenterPoint.x;
        mNeedleCenterPoint.y = 20 + mNeedleBitmap.getWidth() / 6;
    }

    @Override
    protected void onDraw(Canvas canvas) {

        // 2.绘制黑胶唱片外侧半透明边框
        mCoverBorder.setBounds(mDiscPoint.x - mCoverBorderWidth, mDiscPoint.y - mCoverBorderWidth,
                mDiscPoint.x + mDiscBitmap.getWidth() + mCoverBorderWidth, mDiscPoint.y + mDiscBitmap.getHeight() + mCoverBorderWidth);
        mCoverBorder.draw(canvas);
        // 3.绘制黑胶
        // 设置旋转中心和旋转角度，setRotate和preTranslate顺序很重要
        mDiscMatrix.setRotate(mDiscRotation, mDiscCenterPoint.x, mDiscCenterPoint.y);
        // 设置图片起始坐标
        mDiscMatrix.preTranslate(mDiscPoint.x, mDiscPoint.y);
        canvas.drawBitmap(mDiscBitmap, mDiscMatrix, null);
        // 4.绘制封面
        mCoverMatrix.setRotate(mDiscRotation, mCoverCenterPoint.x, mCoverCenterPoint.y);
        mCoverMatrix.preTranslate(mCoverPoint.x, mCoverPoint.y);
        canvas.drawBitmap(mCoverBitmap, mCoverMatrix, null);
        // 5.绘制指针
        mNeedleMatrix.setRotate(mNeedleRotation, mNeedleCenterPoint.x, mNeedleCenterPoint.y);
        mNeedleMatrix.preTranslate(mNeedlePoint.x, mNeedlePoint.y);
        canvas.drawBitmap(mNeedleBitmap, mNeedleMatrix, null);
    }

    public void initNeedle(boolean isPlaying) {
        mNeedleRotation = isPlaying ? NEEDLE_ROTATION_PLAY : NEEDLE_ROTATION_PAUSE;
        invalidate();
    }

    public void setCoverBitmap(Bitmap bitmap) {
        mCoverBitmap = bitmap;
        mDiscRotation = 0.0f;
        invalidate();
    }

    public void start() {
        if (isPlaying) {
            return;
        }
        isPlaying = true;
        mHandler.post(mRotationRunnable);
        mPlayAnimator.start();
    }

    public void pause() {
        if (!isPlaying) {
            return;
        }
        isPlaying = false;
        mHandler.removeCallbacks(mRotationRunnable);
        mPauseAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mNeedleRotation = (float) animation.getAnimatedValue();
        invalidate();
    }

    private Runnable mRotationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying && MyApplication.getInstance().isMusicPrepared()) {
                mDiscRotation += DISC_ROTATION_INCREASE;
                if (mDiscRotation >= 360) {
                    mDiscRotation = 0;
                }
                invalidate();
            }
            mHandler.postDelayed(this, TIME_UPDATE);
        }
    };

    private int dp2px(float dpValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public int getScreenWidth() {
        WindowManager wm = (WindowManager) musicPlayContext.getSystemService(Context.WINDOW_SERVICE);
        return wm.getDefaultDisplay().getWidth();
    }

    public void setRoundLength(int roundLength) {
        if (this.roundLength != roundLength) {
            this.roundLength = roundLength;
        }
    }


    public Bitmap getmCoverBitmap() {
        return mCoverBitmap;
    }

    public void setmCoverBitmap(Bitmap mCoverBitmap) {
        this.mCoverBitmap = mCoverBitmap;
    }
}
