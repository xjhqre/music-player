package com.example.music_player;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

import com.example.music_player.model.MusicBean;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void urltest() throws IOException, JSONException {
        // 实例化OkHttpClient对象
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()

                .url("https://music.163.com/api/search/get?s=" +
                        "藍と極星" + "&type=1&offset=0&limit=5")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data = response.body().string();
        System.out.println("返回的信息：");
        System.out.println(data);


        JSONObject jsonObject = new JSONObject(data);

//        OnlineMusic onlineMusic = new OnlineMusic();

        JSONArray songs = jsonObject.getJSONObject("result").getJSONArray("songs");
        for (int i = 0; i < songs.length(); i++) {
            JSONObject jo = songs.getJSONObject(i);
            System.out.println(jo.getString("name"));
            System.out.println((jo.getInt("id")));

            JSONArray artists = jo.getJSONArray("artists");
            String artistName = artists.getJSONObject(0).getString("name");
            System.out.println(artistName);

            // 专辑
            JSONObject album = jo.getJSONObject("album");
            System.out.println(album.getInt("id"));
            System.out.println(album.getString("name"));

            // 时长
            int duration = jo.getInt("duration");
            System.out.println(duration);

        }

    }

    @Test
    public void test2() throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();

        int id = 34794220;

        Request request = new Request.Builder()
                .url("http://music.163.com/api/album/" + id + "?ext=true&id=" + id + "&offset=0&total=true&limit=10")
                .build();
        Response response = client.newCall(request).execute();
        String data = response.body().string();

        System.out.println(data);

        JSONObject jsonObject = new JSONObject(data);

        JSONArray songs = jsonObject.getJSONObject("album").getJSONArray("songs");
        JSONObject song = songs.getJSONObject(0);
        JSONObject album = song.getJSONObject("album");
        String picUrl = album.getString("picUrl");
        System.out.println(picUrl);

    }
}