package com.peaceb.getfreebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
            action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
            if (Setting.getBoolean(context, Setting.KEY_USE_NOTIFY_NEWBOOK)) {
                Setting.startCheckNewBook(context);
            }
        }
    }
}