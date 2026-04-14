package com.hndl.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.hndl.ui.R;
import com.hndl.ui.base.BaseActivity;
import com.hndl.ui.utils.MMKVUtils;

/**
 * 空载标定
 */
public class EmptyCalibrationActivity extends BaseActivity implements View.OnClickListener {

    private LinearLayout llReturn;
    private TextView tvZeroToFullAdjustment;
    private TextView tvEmptyHookDebugging;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_calibration_layout);
        ImmersionBar.with(this)
                .hideBar(BarHide.FLAG_HIDE_BAR)
                .init();
        initView();
        initData();
    }

    @Override
    public void initView() {

        llReturn = findViewById(R.id.ll_return);
        tvZeroToFullAdjustment = findViewById(R.id.tv_zero_to_full_adjustment);
        tvEmptyHookDebugging = findViewById(R.id.tv_empty_hook_debugging);

        llReturn.setOnClickListener(this);
        tvZeroToFullAdjustment.setOnClickListener(this);
        tvEmptyHookDebugging.setOnClickListener(this);
    }

    @Override
    public void initData() {
        int languageState = MMKVUtils.getInstance().decodeInt("languageState");
        if (languageState!=0){
            tvZeroToFullAdjustment.setTextSize(12);
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
            case R.id.tv_zero_to_full_adjustment:{
                ActivityUtils.startActivity(ZeroToFullAdjustmentActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.tv_empty_hook_debugging:{
                ActivityUtils.startActivity(EmptyHookDebuggingActivity.class);
                overridePendingTransition(0, 0);
                break;
            }
        }
    }
}
