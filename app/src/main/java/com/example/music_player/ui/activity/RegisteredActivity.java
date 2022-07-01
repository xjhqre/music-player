package com.example.music_player.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.music_player.R;
import com.example.music_player.application.MyApplication;
import com.example.music_player.receiver.ExitAppReceiver;
import com.example.music_player.utils.DBOpenHelper;
import com.example.music_player.utils.ToastUtils;
import com.flyco.systembar.SystemBarHelper;

public class RegisteredActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText et_rgsName, et_rgsEmail, et_rgsPhoneNum, et_rgsPsw1, et_rgsPsw2;

    private ExitAppReceiver mExitAppReceiver; // 关闭Activity广播

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);//禁止横屏
        setContentView(R.layout.activity_registered);
        SystemBarHelper.immersiveStatusBar(this, 0);

        // 注册退出广播接收器
        mExitAppReceiver = new ExitAppReceiver(this);
        registerReceiver(mExitAppReceiver,new IntentFilter("action.exit"));

        initView();//初始化界面
    }

    private void initView() {
        et_rgsName = findViewById(R.id.et_rgsName);
        et_rgsPhoneNum = findViewById(R.id.et_rgsPhoneNum);
        et_rgsPsw1 = findViewById(R.id.et_rgsPsw1);
        et_rgsPsw2 = findViewById(R.id.et_rgsPsw2);

        Button btn_register = findViewById(R.id.btn_rgs);
        ImageView iv_back = findViewById(R.id.iv_back);
        /**
         * 注册页面能点击的就三个地方
         * top处返回箭头、刷新验证码图片、注册按钮
         */
        iv_back.setOnClickListener(this);
        btn_register.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back://返回登录界面
                Intent intent = new Intent(RegisteredActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_rgs://注册按钮
                //获取用户输入的用户名、密码、验证码
                String username = et_rgsName.getText().toString().trim();
                String password1 = et_rgsPsw1.getText().toString().trim();
                String password2 = et_rgsPsw2.getText().toString().trim();
                String phonenum = et_rgsPhoneNum.getText().toString().trim();
                //注册验证
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password1) && !TextUtils.isEmpty(password2)) {
                    //判断两次密码是否一致
                    if (password1.equals(password2)) {
                        //将用户名和密码加入到数据库中
                        MyApplication.getInstance().getDbOpenHelper().addUser(username, password2, phonenum);
                        Intent intent1 = new Intent(RegisteredActivity.this, LoginActivity.class);
                        startActivity(intent1);
                        finish();
                        ToastUtils.show("验证通过，注册成功");
                    } else {
                        ToastUtils.show("两次密码不一致");
                    }
                } else {
                    ToastUtils.show("注册信息不完善");
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
