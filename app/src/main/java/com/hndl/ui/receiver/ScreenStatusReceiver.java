package com.hndl.ui.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hndl.ui.AppData;
import com.hndl.ui.utils.AdbUtils;
import com.hndl.ui.utils.LogUtil;

public class ScreenStatusReceiver extends BroadcastReceiver {
    String SCREEN_ON = "android.intent.action.SCREEN_ON";
    String SCREEN_OFF = "android.intent.action.SCREEN_OFF";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SCREEN_ON.equals(intent.getAction())) {
            //屏幕亮做xxx操作
            LogUtil.e("XJW", "屏幕亮做xxx操作");
            if (!AppData.is31) {
                AppData.is31 = true;
                AdbUtils.lowPower(1);
            }
        } else if (SCREEN_OFF.equals(intent.getAction())) {
            //屏幕暗做xxx操作
            LogUtil.e("XJW", "屏幕暗做xxx操作");
        }
    }
}
