package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Surface;

import androidx.annotation.Nullable;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.AdbUtils;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.TimeZone;

public class SplashActivity extends BaseActivity {

    static {
        System.loadLibrary("mmqcar_qcar_jni");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        boolean isFrist= MMKVUtils.getInstance().decodeBoolean("isFrist");
        if (!isFrist){
            Settings.System.putInt(this.getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_270);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Integer.MAX_VALUE);
            android.provider.Settings.System.putString(AppData.getInstance().getContentResolver(),
                    android.provider.Settings.System.TIME_12_24, "24");
            AlarmManager alarm= (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);//获得AlarmManager服务对象
            alarm.setTimeZone("Asia/Shanghai");//设置上海时区
            setLanguage(Locale.CHINESE);
            MMKVUtils.getInstance().encode("isFrist", true);
        }
        Intent intent=new Intent();
        intent.setAction("com.omarea.gesture.SPF_CONFIG");
        intent.setPackage("com.omarea.gesture");
        intent.putExtra("landscape_ios_bar", false);
        intent.putExtra("portrait_ios_bar", false);
        sendBroadcast(intent);
        AppData.mAlarmingList.clear();
        canData_Alarm=null;
    }

    private void setLanguage(Locale mLocale) {
        try {
            Class localPicker = Class.forName("com.android.internal.app.LocalePicker");
            Method updateLocale = localPicker.getDeclaredMethod("updateLocale",
                    Locale.class);
            updateLocale.invoke(null,mLocale);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
                 | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        AppData.isCanData=false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        AdbUtils.adblock();
//        DateUtils.setSysDate(this,2023,8,21);
//        DateUtils.setSysTime(this,10,00);
        AppData.getInstance().voiceState = MMKVUtils.getInstance().decodeInt("voiceState");
        AppData.getInstance().modelType = MMKVUtils.getInstance().decodeInt("modelType");
        AppData.getInstance().workHours = MMKVUtils.getInstance().decodeInt("workHours");
        if (AppData.getInstance().modelType==0){
            AppData.getInstance().modelType=2;
        }
//        byte[] dataFilter = new byte[]{(byte) 0x30, 0x31};
//        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x33, dataFilter));
//        dataFilter = new byte[]{(byte) 0x31, 0x31};
//        canSerialManager.sendBytes(MyUtils.constructSerialFilter((byte) 0x33, dataFilter));
        ThreadUtils.runOnUiThreadDelayed(() -> {
            if (AppData.is31) {
                isConfirm=false;
                AppData.isCanData=true;
                ActivityUtils.startActivity(ConfirmActivity.class);
                finish();
                overridePendingTransition(0, 0);
            }
        }, 200);
    }
}
