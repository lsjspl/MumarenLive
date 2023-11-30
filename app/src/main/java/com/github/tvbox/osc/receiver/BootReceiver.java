package com.github.tvbox.osc.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.github.mr5.live.ui.LivePlayActivity;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
             Intent activityIntent = new Intent(context, LivePlayActivity.class);
             activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             context.startActivity(activityIntent);
        }
    }

}
