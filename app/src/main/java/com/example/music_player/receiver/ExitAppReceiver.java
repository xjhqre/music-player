package com.example.music_player.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ExitAppReceiver extends BroadcastReceiver {
    private Activity activity;

    public ExitAppReceiver(Activity activity){
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        activity.finish();
    }
}
