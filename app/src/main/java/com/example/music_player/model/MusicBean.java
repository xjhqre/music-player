package com.example.music_player.model;

import android.graphics.Bitmap;

import java.io.Serializable;

public class MusicBean implements Serializable {

    private int type; // 歌曲类型:     0:本地     1:网络
    private String songId; //歌曲id
    private String title; //歌曲名称
    private String artist; //歌手名称
    private String album; //专辑名称
    private int albumId; // [本地]专辑ID
    private int duration; //歌曲时长
    private String path; //歌曲路径
    private String albumArt;  //专辑地址
    private String fileName; // [本地]文件名
    Bitmap albumPic;       // 【在线】专辑图片
    private String proxyUrl;  // 本地缓存代理地址
    private long fileSize; // [本地]文件大小
    private boolean isLike;

    public MusicBean() {
    }

    public MusicBean(String songId, String title, String artist, String album, int duration, String path, String albumArt) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.path = path;
        this.albumArt = albumArt;
        this.isLike = false;
    }

    public interface Type {
        int LOCAL = 0;
        int ONLINE = 1;
    }

    public String getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(String albumArt) {
        this.albumArt = albumArt;
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isLike() {
        return isLike;
    }

    public void setLike(boolean like) {
        isLike = like;
    }

    public Bitmap getAlbumPic() {
        return albumPic;
    }

    public void setAlbumPic(Bitmap albumPic) {
        this.albumPic = albumPic;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }
}