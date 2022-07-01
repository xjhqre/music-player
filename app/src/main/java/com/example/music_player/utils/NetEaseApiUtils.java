package com.example.music_player.utils;

import android.util.Log;

import com.example.music_player.model.MusicBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetEaseApiUtils {

    private static final String TAG = "NetEaseApiUtils";

    // 实例化OkHttpClient对象
    static OkHttpClient client = new OkHttpClient();

    // 获取轮播图地址集合和音乐集合
    public static void getBanner(ArrayList<String> arrayList, ArrayList<MusicBean> musicBeans) throws IOException, JSONException {

        Request request = new Request.Builder()
                .url("http://cloud-music.pl-fe.cn/banner?type=1")
                .build();
        Response response;
        response = client.newCall(request).execute();
        String data = response.body().string();
        JSONObject jsonObject = new JSONObject(data);

        System.out.println(data);

        JSONArray banners = jsonObject.getJSONArray("banners");

        for (int i = 0; i < banners.length(); i++) {
            JSONObject banner = banners.getJSONObject(i);
            String pic = banner.getString("pic");
            arrayList.add(pic);

            System.out.println(banner);

            // 封装音乐
            MusicBean bannerMusic = new MusicBean();
            if (banner.isNull("song")) {
                musicBeans.add(null);
                continue;
            }
            JSONObject song = banner.getJSONObject("song");
            String name = song.getString("name");
            String songId = song.getString("id");
            JSONArray artists = song.getJSONArray("ar");
            JSONObject artist = artists.getJSONObject(0);
            String artistName = artist.getString("name");
            JSONObject al = song.getJSONObject("al");
            String albumId = al.getString("id");
            String albumName = al.getString("name");
            String albumArt = al.getString("picUrl");
            String duration = song.getString("dt");
            bannerMusic.setType(1);
            bannerMusic.setSongId(songId);
            bannerMusic.setTitle(name);
            bannerMusic.setArtist(artistName);
            bannerMusic.setAlbum(albumName);
            bannerMusic.setAlbumId(Integer.parseInt(albumId));
            bannerMusic.setDuration(Integer.parseInt(duration));
            bannerMusic.setPath("https://music.163.com/song/media/outer/url?id=" + bannerMusic.getSongId() + ".mp3");
            bannerMusic.setAlbumArt(albumArt);
            bannerMusic.setLike(false);
            musicBeans.add(bannerMusic);
        }
        Log.d(TAG, "getBanner: 获取轮播图完成");
    }

    // 搜索音乐
    public static void searchMusic(String query, ArrayList<MusicBean> arrayList) throws IOException, JSONException {

        Request request = new Request.Builder()
                .url("https://music.163.com/api/search/get?s=" +
                        query + "&type=1&offset=0&limit=10")
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        String data = response.body().string();
        JSONObject jsonObject = new JSONObject(data);

        JSONArray songs = jsonObject.getJSONObject("result").getJSONArray("songs");

        if (songs.length() == 0) {
            return;
        }

        for (int i = 0; i < songs.length(); i++) {

            MusicBean music = new MusicBean();
            JSONObject jo = songs.getJSONObject(i);
            // 歌曲名
            music.setTitle(jo.getString("name"));

            // 歌曲id
            music.setSongId(String.valueOf(jo.getInt("id")));

            // 歌手名
            JSONArray artists = jo.getJSONArray("artists");
            String artistName = artists.getJSONObject(0).getString("name");
            music.setArtist(artistName);

            // 专辑
            JSONObject album = jo.getJSONObject("album");
            // 专辑名
            music.setAlbum(album.getString("name"));
            // 专辑id
            music.setAlbumId(album.getInt("id"));
            // 专辑封面地址
            String netPic = getNetPic(music.getAlbumId());
            music.setAlbumArt(netPic);

            // 时长
            int duration = jo.getInt("duration");
            music.setDuration(duration);

            // 歌曲地址
            music.setPath("https://music.163.com/song/media/outer/url?id=" + music.getSongId() + ".mp3");

            // TODO 是否喜欢，查询数据库判断
            music.setLike(false);

            music.setType(1);

            arrayList.add(music);
        }
        Log.d(TAG, "searchMusic: 封装完毕");

    }

    // 获取专辑封面
    public static String getNetPic(int albumId) throws IOException, JSONException {

        Request request = new Request.Builder()
                .url("https://netease-cloud-music-api-binaryify.vercel.app/album?id=" + albumId)
                .build();
        Response response = client.newCall(request).execute();
        String data = response.body().string();

        JSONObject jsonObject = new JSONObject(data);

        JSONArray songs = jsonObject.getJSONArray("songs");
        JSONObject song = songs.getJSONObject(0);
        JSONObject album = song.getJSONObject("al");
        String picUrl = album.getString("picUrl");
        System.out.println(picUrl);

        return picUrl;
    }
}
