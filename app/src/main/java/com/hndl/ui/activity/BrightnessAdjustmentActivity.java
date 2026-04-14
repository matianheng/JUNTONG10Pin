package com.hndl.ui.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import androidx.annotation.Nullable;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MyUtils;

public class BrightnessAdjustmentActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private LinearLayout llAutoBrightness;
    private Switch switchBrightness;
    private SeekBar seekbarBrightness;

    private boolean isMode=true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.brightness_adjustment_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        llAutoBrightness = findViewById(R.id.ll_auto_brightness);
        switchBrightness = findViewById(R.id.switch_brightness);
        seekbarBrightness = findViewById(R.id.seekbar_brightness);

        llReturn.setOnClickListener(this);
        switchBrightness.setOnClickListener(this);
        llAutoBrightness.setOnClickListener(this);
    }

    @Override
    public void initData() {
        seekbarBrightness.setMax(250);
        seekbarBrightness.setProgress(MyUtils.getScreenBrightness(this));
        seekbarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //关闭光感
                //setScreenManualMode(BrightnessActivity.this);
                ModifySettingsScreenBrightness(BrightnessAdjustmentActivity.this,progress);
                seekbarBrightness.setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        ContentResolver contentResolver = this.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                isMode=true;
                switchBrightness.setChecked(true);
            }else {
                isMode=false;
                switchBrightness.setChecked(false);
            }
        } catch (Settings.SettingNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭光感，设置手动调节背光模式
     * SCREEN_BRIGHTNESS_MODE_AUTOMATIC 自动调节屏幕亮度模式值为1
     * SCREEN_BRIGHTNESS_MODE_MANUAL 手动调节屏幕亮度模式值为0
     **/
    public void setScreenManualMode(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setScreenMode(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        try {
            int mode = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }else {
                Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
//                int autoBrightness = Settings.System.getInt(context.getContentResolver(),
//                        Settings.System.SCREEN_BRIGHTNESS);
//                LogUtil.e("XJW","autoBrightness:"+autoBrightness);
//                seekbarBrightness.setProgress(autoBrightness);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改Setting 中屏幕亮度值  亮度   0-255
     * 修改Setting的值需要动态申请权限 <uses-permission
     * android:name="android.permission.WRITE_SETTINGS"/>
     **/
    private void ModifySettingsScreenBrightness(Context context, int birghtessValue) {
        // 首先需要设置为手动调节屏幕亮度模式
        setScreenManualMode(context);
        ContentResolver contentResolver = context.getContentResolver();
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, birghtessValue);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.switch_brightness:{

            }
            case R.id.ll_auto_brightness:{
                if (!isMode) {
                    setScreenMode(this);
                    isMode=true;
                    switchBrightness.setChecked(true);
                }else {
                    setScreenMode(this);
                    isMode=false;
                    switchBrightness.setChecked(false);
                }
                break;
            }
        }
    }
}
