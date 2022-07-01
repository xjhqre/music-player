package com.example.music_player.model;



import androidx.appcompat.app.AppCompatActivity;

import com.example.music_player.ui.activity.MusicListActivity;


import java.io.Serializable;
import java.util.ArrayList;

public enum MusicSheet implements Serializable {
    STATUSBAR_TINT("我喜欢的音乐", MusicListActivity.class, new ArrayList<>()),
    MUSIC_LIST_ACTIVITY("本地音乐", MusicListActivity.class, new ArrayList<>());

    public String mItem;
    public Class<? extends AppCompatActivity> mClazz;
    public ArrayList<MusicBean> musicList;

    MusicSheet(String item, Class<? extends AppCompatActivity> clazz, ArrayList<MusicBean> musicList) {
        mItem = item;
        mClazz = clazz;
        this.musicList = musicList;
    }
}
