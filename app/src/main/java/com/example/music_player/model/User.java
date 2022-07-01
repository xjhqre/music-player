package com.example.music_player.model;

/**
 * Created by kizai 2020/05/19
 */
public class User {
    private long id;                // 数据库用户id
    private String name;            //用户名
    private String password;        //密码
    private String phonenum;        //手机号码

    public User(long id, String name, String password, String phonenum) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.phonenum = phonenum;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhonenum() {
        return phonenum;
    }

    public void setPhonenum(String phonenum) {
        this.phonenum = phonenum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", phonenum='" + phonenum + '\'' +
                '}';
    }
}


