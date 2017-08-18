package com.wifitest;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.wifitest.services.CheckApService;


public class App extends Application {

    public static final String TAG = App.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        checkService();

    }

    // 如果wifi开关已打开
    private void checkService() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Log.i(TAG, "开启服务");
            wifiManager.setWifiEnabled(true);
        }
        // 开启服务
        startService(new Intent(this, CheckApService.class));
    }
}
