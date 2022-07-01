package com.example.music_player.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.music_player.model.MusicBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 读取本地音乐信息，返回封装好的音乐集合
 */
public class MusicDataUtil {

    // 读取本地音乐信息
    public static void loadMusicData(ContentResolver resolver, List<MusicBean> mDatas) {
        /* 加载本地存储当中的音乐mp3文件到集合当中*/
        // 2.获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 3 开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
        // 4.遍历Cursor
        int id = 0;
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));  // 歌曲的名称
            @SuppressLint("Range") String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));  // 歌曲的专辑名
            id++;
            String sid = String.valueOf(id);
            @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));  // 歌曲文件的全路径
            @SuppressLint("Range") int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            // 获取专辑图片主要是通过album_id进行查询
            @SuppressLint("Range") String album_id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
            String albumArt = getAlbumArt(album_id, resolver);
            // 将一行当中的数据封装到对象当中
            MusicBean bean = new MusicBean(sid, song, singer, album, duration, path, albumArt);
            bean.setType(0);
            mDatas.add(bean);
        }
        cursor.close();

    }

    // 获取音乐封面图片
    public static String getAlbumArt(String album_id, ContentResolver resolver) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{"album_art"};
        Cursor cur = resolver.query(
                Uri.parse(mUriAlbums + "/" + album_id),
                projection, null, null, null);
        String album_art = null;
        if (cur.getCount() > 0 && cur.getColumnCount() > 0) {
            cur.moveToNext();
            album_art = cur.getString(0);
        }
        cur.close();
        return album_art;
    }
}
