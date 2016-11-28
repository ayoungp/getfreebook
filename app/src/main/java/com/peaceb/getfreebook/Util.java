package com.peaceb.getfreebook;

import android.content.Context;
import android.net.wifi.WifiManager;

public class Util {
    public static String getMacAddress(Context context) {
        WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        String mac = manager.getConnectionInfo().getMacAddress();
        return mac;
    }
}
