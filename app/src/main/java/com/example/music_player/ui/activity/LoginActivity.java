package com.example.music_player.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import android.widget.TextView;

import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.receiver.ExitAppReceiver;
import com.example.music_player.model.User;
import com.example.music_player.utils.ToastUtils;
import com.flyco.systembar.SystemBarHelper;

import java.util.ArrayList;

/**
 * 此类 implements View.OnClickListener 之后，
 * 就可以把onClick事件写到onCreate()方法之外
 * 这样，onCreate()方法中的代码就不会显得很冗余
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "LoginActivity";
    private EditText et_User, et_Psw;
    private CheckBox cb_rmbPsw;
    private SharedPreferences.Editor editor;
    MyApplication app;

    private ExitAppReceiver mExitAppReceiver; // 关闭Activity广播


    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//禁止横屏
        setContentView(R.layout.activity_login);
        SystemBarHelper.immersiveStatusBar(this, 0);

        app = (MyApplication) getApplication();

        initView();//初始化界面

        // 注册退出广播接收器
        mExitAppReceiver = new ExitAppReceiver(this);
        registerReceiver(mExitAppReceiver,new IntentFilter("action.exit"));

        SharedPreferences sp = getSharedPreferences("user_mes", MODE_PRIVATE);
        editor = sp.edit();
        if(sp.getBoolean("flag",false)){
            String user_read = sp.getString("user","");
            String psw_read = sp.getString("psw","");
            et_User.setText(user_read);
            et_Psw.setText(psw_read);
        }
    }

    private void initView() {
        //初始化控件
        et_User = findViewById(R.id.et_User);
        et_Psw = findViewById(R.id.et_Psw);
        cb_rmbPsw = findViewById(R.id.cb_rmbPsw);
        Button btn_Login = findViewById(R.id.btn_Login);
        TextView tv_register = findViewById(R.id.tv_Register);
        //设置点击事件监听器
        btn_Login.setOnClickListener(this);
        tv_register.setOnClickListener(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_Register: //注册
                Intent intent = new Intent(LoginActivity.this, RegisteredActivity.class);//跳转到注册界面
                startActivity(intent);
                finish();
                break;
            case R.id.btn_Login:
                String name = et_User.getText().toString().trim();
                String password = et_Psw.getText().toString().trim();
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(password)) {
                    ArrayList<User> data = MyApplication.getInstance().getDbOpenHelper().getAllData();
                    Log.d(TAG, "onClick: 查询用户完成");
                    boolean match = false;    // 匹配用户名和密码
                    boolean match2 = false;   // 匹配是否记住用户名和密码
                    for (int i = 0; i < data.size(); i++) {
                        User user = data.get(i);
                        if ((name.equals(user.getName()) && password.equals(user.getPassword()))||
                                (name.equals(user.getPhonenum())&&password.equals(user.getPassword()))) {
                            match = true;
                            app.setUser(user); // 设置全局用户变量
                            // 记住密码
                            if(cb_rmbPsw.isChecked()){
                                editor.putBoolean("flag",true);
                                editor.putString("user",user.getName());
                                editor.putString("psw",user.getPassword());
                                editor.apply();
                                match2 = true;
                            }else {
                                editor.putString("user",user.getName());
                                editor.putString("psw","");
                                editor.apply();
                                match2 = false;
                            }
                            break;
                        } else {
                            match = false;
                        }
                    }
                    if (match) {
                        if(match2){
                            ToastUtils.show("成功记住密码");
                            cb_rmbPsw.setChecked(true);
                        }
                        ToastUtils.show("登录成功");
                        //用线程启动
                        Thread thread = new Thread(){
                            @Override
                            public void run(){
                                try {
                                    sleep(2000);//2秒 模拟登录时间
                                    Intent intent1 = new Intent(LoginActivity.this, HomeActivity.class);//设置自己跳转到成功的界面

                                    startActivity(intent1);
                                    finish();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        };
                        thread.start();//打开线程
                    } else {
                        ToastUtils.show("用户名或密码不正确，请重新输入");
                    }
                } else {
                    ToastUtils.show("请输入你的用户名或密码");
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播接收器
        unregisterReceiver(mExitAppReceiver);
    }
}
