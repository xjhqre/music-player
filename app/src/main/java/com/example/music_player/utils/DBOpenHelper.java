package com.example.music_player.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.music_player.model.MusicBean;
import com.example.music_player.model.User;

import java.time.Duration;
import java.util.ArrayList;

public class DBOpenHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBOpenHelper";
    /**
     * 声明一个AndroidSDK自带的数据库变量db
     */
    private SQLiteDatabase db;

    public DBOpenHelper(Context context) {
        super(context, "music_db_1", null, 2);
        db = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 用户表
        db.execSQL("CREATE TABLE IF NOT EXISTS user(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "password TEXT," +
                "phonenum TEXT)"
        );

        // 在线音乐表，依赖用户表
        db.execSQL("CREATE TABLE IF NOT EXISTS net_music(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT," +
                "song_id TEXT," +
                "title TEXT," +
                "artist TEXT," +
                "album_name TEXT," +
                "album_id TEXT," +
                "duration TEXT," +
                "path TEXT," +
                "proxy_url TEXT," +
                "album_art TEXT," +
                "is_like INTEGER)"
        );

    }

    //版本适应
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS user");
        db.execSQL("DROP TABLE IF EXISTS net_music");
        onCreate(db);
    }


    public void addUser(String name, String password, String phonenum) {
        db.execSQL("INSERT INTO user (name,password,phonenum) VALUES(?,?,?)", new Object[]{name, password, phonenum});
    }

    public void delete(String name, String password) {
        db.execSQL("DELETE FROM user WHERE name = AND password =" + name + password);
    }

    public void updata(String password) {
        db.execSQL("UPDATE user SET password=?", new Object[]{password});
    }

    /********************************在线音乐**************************************/

    public void addNetMusic(String userId, MusicBean musicBean) {
        db.execSQL("INSERT INTO net_music (user_id,song_id,title,artist,album_name,album_id,duration,path,album_art,proxy_url,is_like) VALUES(?,?,?,?,?,?,?,?,?,?,?)", new Object[]{userId, musicBean.getSongId(), musicBean.getTitle(), musicBean.getArtist(), musicBean.getAlbum(), musicBean.getAlbumId(), musicBean.getDuration(), musicBean.getPath(), musicBean.getAlbumArt(), musicBean.getProxyUrl(), 1});
    }

    public void unlikeNetMusic(String userId, String songId) {
        db.execSQL("UPDATE net_music SET is_like=0 where user_id = ? and song_id = ?", new Object[]{userId, songId});
    }

    public void relikeNetMusic(String userId, String songId) {
        db.execSQL("UPDATE net_music SET is_like=1 where user_id = ? and song_id = ?", new Object[]{userId, songId});
    }

    @SuppressLint("Recycle")
    public boolean searchNetMusicExistById(String userId, String songId) {
        Cursor cursor;
        cursor = db.rawQuery("SELECT is_like FROM net_music where user_id = ? and song_id = ? limit 1", new String[]{userId, songId});
        if (cursor.getCount() == 0) {
            return false;
        }
        cursor.moveToFirst();
        @SuppressLint("Range") int is_like= cursor.getInt(cursor.getColumnIndex("is_like"));
        cursor.close();
        if (is_like == 0) {
            return false;
        }
        return true;
    }


    public ArrayList<User> getAllData() {
        ArrayList<User> list = new ArrayList<>();
        @SuppressLint("Recycle") Cursor cursor = db.query("user", null, null, null, null, null, "name DESC");
        while (cursor.moveToNext()) {
            long id = cursor.getColumnIndex("_id");
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("name"));
            @SuppressLint("Range") String phonenum = cursor.getString(cursor.getColumnIndex("phonenum"));
            @SuppressLint("Range") String password = cursor.getString(cursor.getColumnIndex("password"));

            list.add(new User(id, name, password, phonenum));
        }
        return list;
    }

    // 获取用户喜欢的音乐
    public ArrayList<MusicBean> getNetMusics(String userId) {
        ArrayList<MusicBean> list = new ArrayList<>();
        @SuppressLint("Recycle") Cursor cursor = db.query("net_music", null, null, null, null, null, "_id DESC");
        while (cursor.moveToNext()) {
            @SuppressLint("Range") String user_id = cursor.getString(cursor.getColumnIndex("user_id"));
            @SuppressLint("Range") int is_like = cursor.getInt(cursor.getColumnIndex("is_like"));
            if (userId.equals(user_id) && is_like == 1) {
                @SuppressLint("Range") String song_id = cursor.getString(cursor.getColumnIndex("song_id"));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex("artist"));
                @SuppressLint("Range") String album_name = cursor.getString(cursor.getColumnIndex("album_name"));
                @SuppressLint("Range") String album_id = cursor.getString(cursor.getColumnIndex("album_id"));
                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex("duration"));
                @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex("path"));
                @SuppressLint("Range") String album_art = cursor.getString(cursor.getColumnIndex("album_art"));
                @SuppressLint("Range") String proxy_url = cursor.getString(cursor.getColumnIndex("proxy_url"));

                MusicBean musicBean = new MusicBean(song_id, title, artist, album_name, Integer.parseInt(duration), path, album_art);
                musicBean.setType(1);
                musicBean.setProxyUrl(proxy_url);
                musicBean.setAlbumId(Integer.parseInt(album_id));
                musicBean.setLike(true);
                list.add(musicBean);
            }
        }
        return list;
    }

}

