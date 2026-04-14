package com.hndl.ui.activity;

import static com.hndl.ui.utils.MyUtils.constructSerialData;
import static com.hndl.ui.utils.MyUtils.getInteger;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.AppData;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.LogUtil;
import com.hndl.ui.utils.MMKVUtils;
import com.hndl.ui.utils.MyUtils;

import java.util.Arrays;

/**
 * 状态查询
 */
public class StatusQueryActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvFaultAlarm;
    private TextView tvOvercurrentAlarm;
    private TextView tvInputOutputStatus;
    private TextView tvBrightnessAdjustment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status_query_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvFaultAlarm = findViewById(R.id.tv_fault_alarm);
        tvOvercurrentAlarm = findViewById(R.id.tv_overcurrent_alarm);
        tvInputOutputStatus = findViewById(R.id.tv_input_output_status);
        tvBrightnessAdjustment= findViewById(R.id.tv_brightness_adjustment);

        llReturn.setOnClickListener(this);
        tvFaultAlarm.setOnClickListener(this);
        tvOvercurrentAlarm.setOnClickListener(this);
        tvInputOutputStatus.setOnClickListener(this);
        tvBrightnessAdjustment.setOnClickListener(this);
    }

    @Override
    public void initData() {
        if (AppData.dataSrc==2){
            tvOvercurrentAlarm.setVisibility(View.VISIBLE);
        }else {
            tvOvercurrentAlarm.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_return:{
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_fault_alarm:{
                ActivityUtils.startActivity(FaultAlarmActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_overcurrent_alarm:{
                ActivityUtils.startActivity(OvercurrentAlarmActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_input_output_status:{
                ActivityUtils.startActivity(InputOutputStatusActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_brightness_adjustment:{
                ActivityUtils.startActivity(BrightnessAdjustmentActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
        }
    }
}
