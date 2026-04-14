package com.hndl.ui.activity;

import static com.hndl.ui.AppData.canSerialManager;
import static com.hndl.ui.utils.MyUtils.constructSerialData;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.widget.HintDialog;

import java.util.Arrays;

/**
 * 功能选择
 */
public class FunctionSelectionActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvEmptyCalibration;
    private TextView tvOverloadCalibration;
    private TextView tvSensorCalibrate;
    private TextView tvSensorView;
    private TextView tvRestoreFactorySettings;

    private int hint = -1;
    private HintDialog hintDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.function_selection_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
        hintDialog=new HintDialog(this, getString(R.string.tips), "", new HintDialog.HintDialogListener() {
            @Override
            public void onClick(boolean isConfirm) {
                if (isConfirm&&hint==1){
                    createLoadingDialog("");
                    startRepeatingTask();
                }
            }
        });
    }

    private Handler mHandler = new Handler();
    private static final int DELAY_TIME_MS = 100; // 延迟时间
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            // 在这里执行你的任务
            byte[] data = new byte[]{(byte) 0x23, 0x11, 0x10, 0x01, (byte) 0x6C, (byte) 0x6F, (byte) 0x61, 0x64};
            canSerialManager.sendBytes(constructSerialData((byte) 0x35, 0x600+ AppData.nodeID, data));
            // 重新安排任务以进行下一次执行
            mHandler.postDelayed(mTask, DELAY_TIME_MS);
        }
    };

    private void startRepeatingTask() {
        mHandler.post(mTask);
    }

    private void stopRepeatingTask() {
        if (mTask!=null) {
            mHandler.removeCallbacks(mTask);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
        mHandler=null;
    }

    private byte[] canData_584;
    @Override
    public void getCanData_584(byte[] canData) {
        super.getCanData_584(canData);
        //if (!Arrays.equals(canData, canData_584)) {
            ThreadUtils.runOnUiThread(() -> {
                canData_584 = canData;
                if (canData[0] == 0x60 && canData[1] == 0x11 && canData[2] == 0x10&& canData[3] == 0x01) {
                    stopRepeatingTask();
                    dismissLoadingDialog();
                    showDialog(-1,2,getString(R.string.reset_success));
                }
            });
        //}
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvEmptyCalibration = findViewById(R.id.tv_empty_calibration);
        tvOverloadCalibration = findViewById(R.id.tv_overload_calibration);
        tvSensorCalibrate = findViewById(R.id.tv_sensor_calibrate);
        tvSensorView = findViewById(R.id.tv_sensor_view);
        tvRestoreFactorySettings = findViewById(R.id.tv_restore_factory_settings);

        llReturn.setOnClickListener(this);
        tvEmptyCalibration.setOnClickListener(this);
        tvOverloadCalibration.setOnClickListener(this);
        tvSensorCalibrate.setOnClickListener(this);
        tvRestoreFactorySettings.setOnClickListener(this);
        tvSensorView.setOnClickListener(this);
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_empty_calibration:{
                ActivityUtils.startActivity(EmptyCalibrationActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_overload_calibration:{
                ActivityUtils.startActivity(OverloadCalibrationActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_sensor_calibrate:{
                ActivityUtils.startActivity(SensorCalibrateActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_restore_factory_settings:{
                showDialog(1,3,getString(R.string.confirm_factory_reset));
                break;
            }
            case R.id.tv_sensor_view:{
                ActivityUtils.startActivity(SensorViewActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
        }
    }

    private void showDialog(int h, int type, String content) {
        hint = h;
        hintDialog.setButtonVisibility(type);
        hintDialog.setContent(content);
        hintDialog.setCancelable(false);
        hintDialog.show();
    }
}
