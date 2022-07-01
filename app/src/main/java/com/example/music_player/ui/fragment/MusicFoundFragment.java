package com.example.music_player.ui.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;

import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.bigkoo.convenientbanner.holder.Holder;
import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.model.MusicBean;
import com.example.music_player.ui.activity.HomeActivity;
import com.example.music_player.ui.activity.MusicPlayActivity;
import com.example.music_player.ui.activity.NetMusicSearchActivity;
import com.example.music_player.utils.NetEaseApiUtils;
import com.example.music_player.utils.ToastUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class MusicFoundFragment extends Fragment {


//    @BindView(R.id.fragment_music_found_navigation)
//    LinearLayout fragment_music_found_navigation;

    // 是否自动轮播,控制如果是一张图片，不能滑动
    private boolean mCanLoop = true;

    @BindView(R.id.fragment_music_found_toolbar)
    Toolbar fragment_music_found_toolbar;

    @BindView(R.id.fragment_music_found_navigation)
    ImageView fragment_music_found_navigation;

    @BindView(R.id.fragment_music_found_convenientBanner)
    ConvenientBanner convenientBanner;

    private SearchView mSearchView;

    // 轮播图地址集合
    ArrayList<String> bannerList = new ArrayList<>();

    // 轮播图音乐集合
    ArrayList<MusicBean> bannerMusicList = new ArrayList<>();

    private static String TAG = "";

    private Context mContext;

    boolean prepared = false;

    public MusicFoundFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mContext = getActivity();
        View view = inflater.inflate(R.layout.fragment_music_found, container, false);
        view.setClickable(true);// 防止点击穿透，底层的fragment响应上层点击触摸事件
        ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(fragment_music_found_toolbar);


        // http获取网络图片
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    NetEaseApiUtils.getBanner(bannerList, bannerMusicList);
                    // 通知主线程banner获取完毕
                    mHandler.sendEmptyMessage(0);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        // 侧边导航栏点击事件
        fragment_music_found_navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).toggleDrawer();
                }
            }
        });

        return view;

    }

    private void initBanner() {
        convenientBanner.setPages(new CBViewHolderCreator() {
            @Override
            public Holder createHolder(View itemView) {
                return new NetImageHolderView(itemView);
            }

            @Override
            public int getLayoutId() {
                return R.layout.item_banner;
            }
        }, bannerList)
                .setPageIndicator(new int[]{R.mipmap.ic_page_indicator, R.mipmap.ic_page_indicator_focused})
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL)
                .setPointViewVisible(mCanLoop)
                .setCanLoop(mCanLoop)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        if (bannerMusicList.get(position) != null) {
                            MyApplication.getInstance().setMusicBean(bannerMusicList.get(position));
                            MyApplication.getInstance().getPlaylist().add(0, bannerMusicList.get(position));
                            MyApplication.getInstance().setCur_position(0);

                            // 传递给MusicFragment的数据
                            Intent intent = new Intent(getActivity(), MusicPlayActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_search);
        //通过MenuItem得到SearchView
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        mSearchView.setQueryHint("输入要查询的歌曲名称");
        // 取出搜索框下划线
        mSearchView.findViewById(androidx.appcompat.R.id.search_plate).setBackground(null);
        mSearchView.findViewById(androidx.appcompat.R.id.submit_area).setBackground(null);

        // 设置搜索监听事件
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(mContext, NetMusicSearchActivity.class);
                intent.putExtra("song_name", query);
                startActivity(intent);
                ToastUtils.show("正在搜索歌曲");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);

    }

    // 接收消息，刷新界面
    @SuppressLint("HandlerLeak")
    public Handler mHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                initBanner();
                prepared = true;
                onResume();
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //开始执行轮播，并设置轮播时长
        if (prepared) {
            convenientBanner.startTurning(5000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (convenientBanner.isTurning()) {
            convenientBanner.stopTurning();
        }
    }


    /**
     * 轮播图 对应的holder
     */
    public class NetImageHolderView extends Holder<String> {
        private ImageView mImageView;

        //构造器
        public NetImageHolderView(View itemView) {
            super(itemView);
        }

        @Override
        protected void initView(View itemView) {
            //找到对应展示图片的imageview
            mImageView = itemView.findViewById(R.id.iv_banner);
            //设置图片加载模式为铺满，具体请搜索 ImageView.ScaleType.FIT_XY
            mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        @Override
        public void updateUI(String data) {
            //使用glide加载更新图片
            Glide.with(requireActivity()).clear(mImageView);
            Glide.with(requireActivity())
                    .load(data)
                    .centerCrop()
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(10, 0)))
                    .into(mImageView);
        }
    }
}