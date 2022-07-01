package com.example.music_player.ui.fragment;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.common.adapter.BaseAdapter;
import com.example.music_player.common.adapter.SingleTypeAdapter;
import com.example.music_player.model.MusicSheet;
import com.example.music_player.ui.activity.HomeActivity;
import com.example.music_player.utils.MusicDataUtil;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MusicSheetFragment extends Fragment {

    @BindView(R.id.recycler_view_music_sheet)
    RecyclerView mRecyclerView;

    @BindView(R.id.fragment_music_sheet_navigation)
    LinearLayout mNavigation;

    private Context mContext;

    MyApplication app;


    public MusicSheetFragment() {
        // Required empty public constructor
    }

    public static MusicSheetFragment newInstance() {
        return new MusicSheetFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getActivity();
        app = (MyApplication) ((Activity) mContext).getApplication();


        View view = inflater.inflate(R.layout.fragment_music_sheet, container, false);
        view.setClickable(true);// 防止点击穿透，底层的fragment响应上层点击触摸事件
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // 加载本地音乐
        app.getLocalMusicLists().clear();
        ContentResolver resolver = mContext.getContentResolver();
        MusicDataUtil.loadMusicData(resolver, app.getLocalMusicLists());

        // TODO 加载我喜欢的音乐
        app.getNetMusicLists().clear();
        app.setNetMusicLists(MyApplication.getInstance().getDbOpenHelper().getNetMusics(String.valueOf(app.getUser().getId())));

        showUi(Arrays.asList(MusicSheet.values()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                if (activity instanceof HomeActivity) {
                    ((HomeActivity) activity).toggleDrawer();
                }
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void showUi(List<MusicSheet> musicSheets) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mRecyclerView.setAdapter(new SingleTypeAdapter<MusicSheet>(musicSheets, R.layout.item_music_sheet) {
            @Override
            public void bindView(final BaseAdapter.ViewHolder holder, int position, View itemView) {
                TextView item = itemView.findViewById(R.id.item_music_sheet_music_name);
                TextView count = itemView.findViewById(R.id.item_music_sheet_music_count);
                ShapeableImageView image = itemView.findViewById(R.id.item_music_sheet_music_image);
                MusicSheet musicSheet = getDataList().get(holder.getAdapterPosition());
                if (holder.getAdapterPosition() == 1) {
                    musicSheet.musicList = app.getLocalMusicLists();
                }
                if (holder.getAdapterPosition() == 0){
                    musicSheet.musicList = app.getNetMusicLists();
                }
                item.setText(musicSheet.mItem);
                count.setText(musicSheet.musicList.size() + "首");
                // 歌单图片
                if (musicSheet.musicList.size() > 0 && holder.getAdapterPosition() == 1) {
                    String albumArt = app.getLocalMusicLists().get(0).getAlbumArt();
                    Bitmap bm = BitmapFactory.decodeFile(albumArt);
                    image.setImageBitmap(bm);
                }
                if (musicSheet.musicList.size() > 0 && holder.getAdapterPosition() == 0) {
                    String albumArt = app.getNetMusicLists().get(0).getAlbumArt();
                    Glide.with(getActivity()).load(albumArt).into(image);
                }
            }

            @Override
            public void handleClick(final BaseAdapter.ViewHolder holder) {
                holder.getItemView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MusicSheet pager = getDataList().get(holder.getAdapterPosition());
                        if(holder.getAdapterPosition() == 0) { // 我喜欢的音乐
                            MyApplication.getInstance().setCurMusicLists(app.getNetMusicLists());
                            MyApplication.getInstance().setCurSheetTitle("我喜欢的音乐");
                        }
                        if(holder.getAdapterPosition() == 1) { // 本地
                            MyApplication.getInstance().setCurMusicLists(app.getLocalMusicLists());
                            MyApplication.getInstance().setCurSheetTitle("本地音乐");
                        }
                        startActivity(new Intent(getActivity(), pager.mClazz));
                    }
                });
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            showUi(Arrays.asList(MusicSheet.values()));
        }
    }
}